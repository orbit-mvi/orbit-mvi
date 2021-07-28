package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Subscribed
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Unsubscribed

internal class DelayingSubscribedCounter(
    private val coroutineScope: CoroutineScope,
    private val repeatOnSubscribedStopTimeout: Long
) : SubscribedCounter {

    private val _subscribedChannel = Channel<Subscription>()
    private val _subscribed: MutableStateFlow<Subscription> = MutableStateFlow(Unsubscribed)
    override val subscribed: Flow<Subscription> = _subscribed.asStateFlow()
    private val initialised = atomic(false)

    private val counter = atomic(0)
    private var mutex = Mutex()

    fun initialise() {
        if (initialised.compareAndSet(expect = false, update = true)) {
            coroutineScope.launch {
                @Suppress("EXPERIMENTAL_API_USAGE")
                _subscribedChannel.receiveAsFlow().mapLatest {
                    if (!it.isSubscribed) {
                        delay(repeatOnSubscribedStopTimeout)
                    }
                    it
                }.collect { _subscribed.emit(it) }
            }
        }
    }

    override suspend fun increment(): Unit = mutex.withLock {
        initialise()
        counter.incrementAndGet()
        _subscribedChannel.send(Subscribed)
    }

    override suspend fun decrement(): Unit = mutex.withLock {
        if (counter.decrementAndGet() == 0) {
            _subscribedChannel.send(Unsubscribed)
        }
    }
}
