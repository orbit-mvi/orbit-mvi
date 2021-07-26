package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public class SubscribedCounter(private val repeatOnSubscribedStopTimeout: Long) {
    private val _subscribed: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @Suppress("EXPERIMENTAL_API_USAGE")
    public val subscribed: Flow<Boolean> = _subscribed.mapLatest {
        if (!it) {
            delay(repeatOnSubscribedStopTimeout)
        }
        it
    }.distinctUntilChanged()

    private val counter = atomic(0)

    private var mutex = Mutex()

    public suspend fun increment(): Unit = mutex.withLock {
        counter.incrementAndGet()
        _subscribed.value = true
    }

    public suspend fun decrement(): Unit = mutex.withLock {
        if (counter.decrementAndGet() == 0) {
            _subscribed.value = false
        }
    }
}
