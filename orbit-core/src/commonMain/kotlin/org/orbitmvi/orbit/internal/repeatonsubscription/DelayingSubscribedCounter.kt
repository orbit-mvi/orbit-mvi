package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Subscribed
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Unsubscribed

internal class DelayingSubscribedCounter(
    private val repeatOnSubscribedStopTimeout: Long
) : SubscribedCounter {

    private val _subscribed: MutableStateFlow<Subscription> = MutableStateFlow(Unsubscribed)

    @Suppress("EXPERIMENTAL_API_USAGE")
    public override val subscribed: Flow<Subscription> = _subscribed.mapLatest {
        if (!it.isSubscribed) {
            delay(repeatOnSubscribedStopTimeout)
        }
        it
    }.distinctUntilChanged()

    private val counter = atomic(0)
    private var mutex = Mutex()

    override suspend fun increment(): Unit = mutex.withLock {
        counter.incrementAndGet()
        _subscribed.value = Subscribed
    }

    override suspend fun decrement(): Unit = mutex.withLock {
        if (counter.decrementAndGet() == 0) {
            _subscribed.value = Unsubscribed
        }
    }
}
