package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public class SubscribedCounter(scope: CoroutineScope, private val repeatOnSubscribedStopTimeout: Long) {
    private val _subscribed = Channel<Boolean>()

    @Suppress("EXPERIMENTAL_API_USAGE")
    public val subscribed: StateFlow<Boolean> = _subscribed
        .receiveAsFlow()
        .mapLatest {
            if (!it) {
                delay(repeatOnSubscribedStopTimeout)
            }
            it
        }
        .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = false)

    private val counter = atomic(0)

    private var mutex = Mutex()

    public suspend fun increment(): Unit = mutex.withLock {
        counter.incrementAndGet()
        _subscribed.send(true)
    }

    public suspend fun decrement(): Unit = mutex.withLock {
        if (counter.decrementAndGet() == 0) {
            _subscribed.send(false)
        }
    }
}
