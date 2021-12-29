package org.orbitmvi.orbit.internal

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

internal fun <T> StateFlow<T>.onSubscribe(block: () -> Unit): StateFlow<T> =
    object : StateFlow<T> {
        override val replayCache: List<T>
            get() = this@onSubscribe.replayCache

        override val value: T
            get() = this@onSubscribe.value

        override suspend fun collect(collector: FlowCollector<T>): Nothing {
            block()
            this@onSubscribe.collect(collector)
        }
    }
