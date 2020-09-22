/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2

import kotlinx.coroutines.delay
import java.io.Closeable

/**
 * Allows you to record all observed values of a stream for easy testing.
 *
 * @param stream The stream to observe.
 */
@Suppress("DEPRECATION")
class TestStreamObserver<T>(stream: Stream<T>) {
    private val _values = mutableListOf<T>()
    private val closeable: Closeable
    val values: List<T>
        get() = _values

    init {
        closeable = stream.observe {
            _values.add(it)
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
    fun close(): Unit = closeable.close()

    companion object {
        private const val AWAIT_TIMEOUT_MS = 10L
    }
}
