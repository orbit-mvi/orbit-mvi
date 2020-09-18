package com.babylon.orbit2

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Allows you to record all observed values of a flow for easy testing.
 *
 * @param flow The flow to observe.
 */
class TestFlowObserver<T>(flow: Flow<T>) {
    private val _values = mutableListOf<T>()
    private val closeable: Job
    val values: List<T>
        get() = _values

    init {
        closeable = GlobalScope.launch {
            flow.collect {
                _values.add(it)
            }
        }
    }

    /**
     * Awaits until the specified count of elements has been received or the timeout is hit.
     *
     * @param count The awaited element count.
     * @param timeout How long to wait for in milliseconds
     */
    fun awaitCount(count: Int, timeout: Long = 5000L) {
        val start = System.currentTimeMillis()
        while (values.count() < count) {
            if (System.currentTimeMillis() - start > timeout) {
                break
            }
            Thread.sleep(AWAIT_TIMEOUT_MS)
        }
    }

    /**
     * Awaits until the specified count of elements has been received or the timeout is hit.
     *
     * @param count The awaited element count.
     * @param timeout How long to wait for in milliseconds
     */
    suspend fun awaitCountSuspending(count: Int, timeout: Long = 5000L) {
        val start = System.currentTimeMillis()
        while (values.count() < count) {
            if (System.currentTimeMillis() - start > timeout) {
                break
            }
            delay(AWAIT_TIMEOUT_MS)
        }
    }

    /**
     * Closes the subscription on the underlying stream. No further values will be received after
     * this call.
     */
    fun close(): Unit = closeable.cancel()

    companion object {
        private const val AWAIT_TIMEOUT_MS = 10L
    }
}
