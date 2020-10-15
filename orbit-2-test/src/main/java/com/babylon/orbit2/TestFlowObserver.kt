package com.babylon.orbit2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Allows you to record all observed values of a flow for easy testing.
 *
 * @param flow The flow to observe.
 */
class TestFlowObserver<T>(flow: Flow<T>) {
    private val _values = mutableListOf<T>()
    private val scope = CoroutineScope(Dispatchers.Unconfined)
    val values: List<T>
        get() = _values

    init {
        scope.launch {
            flow.collect {
                _values.add(it)
            }
        }
    }

    /**
     * Awaits until the specified condition is fulfilled or the timeout is hit.
     *
     * @param timeout How long to wait for in milliseconds
     * @param condition The awaited condition
     */
    suspend fun awaitFor(timeout: Long = 5000L, condition: TestFlowObserver<T>.() -> Boolean) {
        val start = System.currentTimeMillis()
        while (!this.condition()) {
            if (System.currentTimeMillis() - start > timeout) {
                break
            }
            delay(AWAIT_TIMEOUT_MS)
        }
    }

    /**
     * Awaits until the specified count of elements has been received or the timeout is hit.
     *
     * @param count The awaited element count.
     * @param timeout How long to wait for in milliseconds
     */
    fun awaitCount(count: Int, timeout: Long = 5000L) {
        runBlocking {
            withContext(Dispatchers.Default) {
                awaitFor(timeout) { values.size == count }
            }
        }
    }

    /**
     * Awaits until the specified count of elements has been received or the timeout is hit.
     *
     * @param count The awaited element count.
     * @param timeout How long to wait for in milliseconds
     */
    suspend fun awaitCountSuspending(count: Int, timeout: Long = 5000L) = awaitFor(timeout) { values.size == count }

    /**
     * Closes the subscription on the underlying stream. No further values will be received after
     * this call.
     */
    fun close(): Unit = scope.cancel()

    companion object {
        private const val AWAIT_TIMEOUT_MS = 10L
    }
}
