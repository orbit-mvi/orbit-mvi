package org.orbitmvi.orbit.viewmodel

import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
internal fun <T> StateFlow<T>.onEach(block: (T) -> Unit): StateFlow<T> =
    object : AbstractFlow<T>(), StateFlow<T> {
        override val replayCache: List<T>
            get() = this@onEach.replayCache

        override val value: T
            get() = this@onEach.value

        override suspend fun collectSafely(collector: FlowCollector<T>) {
            this@onEach.collect {
                block(it)
                collector.emit(it)
            }
        }
    }
