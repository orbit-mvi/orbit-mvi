package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Subscribed
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Unsubscribed

internal class DelayingSubscribedCounter(
    private val scope: CoroutineScope,
    private val repeatOnSubscribedStopTimeout: Long
) : SubscribedCounter {

    private val _subscribed: MutableStateFlow<Subscription> = MutableStateFlow(Unsubscribed)

    @Suppress("EXPERIMENTAL_API_USAGE")
    override val subscribed: Flow<Subscription> = _subscribed.asStateFlow()

    private val counter = atomic(0)
    private var mutex = Mutex()
    private val job = atomic<Job?>(null)

    override suspend fun increment(): Unit = mutex.withLock {
        counter.incrementAndGet()
        job.getAndSet(null)?.cancel()
        _subscribed.value = Subscribed
    }

    override suspend fun decrement(): Unit = mutex.withLock {
        if (counter.updateAndGet { if (it > 0) it - 1 else 0 } == 0) {
            job.getAndSet(scope.launch {
                delay(repeatOnSubscribedStopTimeout)
                _subscribed.value = Unsubscribed
            })?.cancel()
        }
    }
}
