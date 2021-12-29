package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

private class RefCountFlow<T>(
    private val subscribedCounter: SubscribedCounter,
    private val upStream: Flow<T>
) : Flow<T> {

    override suspend fun collect(collector: FlowCollector<T>) {
        try {
            subscribedCounter.increment()
            upStream.collect(collector)
        } finally {
            subscribedCounter.decrement()
        }
    }
}

internal fun <T> Flow<T>.refCount(subscribedCounter: SubscribedCounter): Flow<T> =
    RefCountFlow(subscribedCounter, this)
