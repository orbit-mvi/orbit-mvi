/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.getAndUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.annotation.OrbitInternal

/**
 * Allows you to record all observed values of a flow for easy testing.
 *
 * @param flow The flow to observe.
 */
public class TestFlowObserver<T>(flow: Flow<T>) {
    private val _values = atomic(emptyList<T>())
    private val scope = CoroutineScope(Dispatchers.Unconfined)
    public val values: List<T>
        get() = _values.value

    init {
        scope.launch {
            flow.collect { emission ->
                _values.getAndUpdate { it + emission }
            }
        }
    }

    /**
     * Awaits until the specified condition is fulfilled or the timeout is hit.
     *
     * @param timeout How long to wait for in milliseconds
     * @param condition The awaited condition
     */
    public suspend fun awaitFor(timeout: Long = 5000L, condition: TestFlowObserver<T>.() -> Boolean) {
        val start = getSystemTimeInMillis()
        while (!this.condition()) {
            if (getSystemTimeInMillis() - start > timeout) {
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
    public fun awaitCount(count: Int, timeout: Long = 5000L) {
        runBlocking {
            awaitFor(timeout) { values.size == count }
        }
    }

    /**
     * Awaits until the specified count of elements has been received or the timeout is hit.
     *
     * @param count The awaited element count.
     * @param timeout How long to wait for in milliseconds
     */
    public suspend fun awaitCountSuspending(count: Int, timeout: Long = 5000L): Unit = awaitFor(timeout) {
        values.size == count
    }

    /**
     * Closes the subscription on the underlying stream. No further values will be received after
     * this call.
     */
    public fun close(): Unit = scope.cancel()

    public companion object {
        private const val AWAIT_TIMEOUT_MS = 10L
    }
}

/**
 * Allows you to put a [Flow] into test mode.
 */
@OrbitInternal
public fun <T> Flow<T>.testFlowObserver(): TestFlowObserver<T> = TestFlowObserver(this)
