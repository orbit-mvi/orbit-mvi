package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

private class RefCountStateFlow<T>(
    private val subscribedCounter: SubscribedCounter,
    private val upStream: StateFlow<T>
) : StateFlow<T> {

    override val replayCache: List<T>
        get() = upStream.replayCache

    override val value: T
        get() = upStream.value

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>) {
        try {
            subscribedCounter.increment()
            upStream.collect(collector)
        } finally {
            subscribedCounter.decrement()
        }
    }
}

internal fun <T> StateFlow<T>.refCount(subscribedCounter: SubscribedCounter): StateFlow<T> =
    RefCountStateFlow(subscribedCounter, this)
