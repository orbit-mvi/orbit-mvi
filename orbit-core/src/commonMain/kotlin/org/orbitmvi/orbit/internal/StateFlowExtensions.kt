package org.orbitmvi.orbit.internal

import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
internal fun <T> StateFlow<T>.onSubscribe(block: () -> Unit): StateFlow<T> =
    object : AbstractFlow<T>(), StateFlow<T> {
        override val replayCache: List<T>
            get() = this@onSubscribe.replayCache

        override val value: T
            get() = this@onSubscribe.value

        override suspend fun collectSafely(collector: FlowCollector<T>) {
            block()
            this@onSubscribe.collect {
                collector.emit(it)
            }
        }
    }
