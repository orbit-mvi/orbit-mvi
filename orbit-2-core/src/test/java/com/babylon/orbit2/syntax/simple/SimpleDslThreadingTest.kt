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

package com.babylon.orbit2.syntax.simple

import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.internal.RealContainer
import com.babylon.orbit2.test
import io.kotest.matchers.string.shouldStartWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.CountDownLatch
import kotlin.random.Random
import kotlin.test.Test

internal class SimpleDslThreadingTest {

    companion object {
        const val ORBIT_THREAD_PREFIX = "orbit"
        const val BACKGROUND_THREAD_PREFIX = "IO"
    }

    @Test
    fun `reducer executes on orbit dispatcher`() {
        val action = Random.nextInt()
        val middleware = BaseDslMiddleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.reducer(action)

        testFlowObserver.awaitCount(2)
        middleware.threadName.shouldStartWith(ORBIT_THREAD_PREFIX)
    }

    @Test
    fun `transformer executes on orbit dispatcher`() {
        val action = Random.nextInt()
        val middleware = BaseDslMiddleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.transformer(action)

        testFlowObserver.awaitCount(2)
        middleware.threadName.shouldStartWith(ORBIT_THREAD_PREFIX)
    }

    private data class TestState(val id: Int)

    private class BaseDslMiddleware : ContainerHost<TestState, String> {

        @Suppress("EXPERIMENTAL_API_USAGE")
        override val container = RealContainer<TestState, String>(
            initialState = TestState(42),
            parentScope = CoroutineScope(Dispatchers.Unconfined),
            settings = Container.Settings(
                orbitDispatcher = newSingleThreadContext(ORBIT_THREAD_PREFIX),
                backgroundDispatcher = newSingleThreadContext(BACKGROUND_THREAD_PREFIX)
            )
        )
        lateinit var threadName: String
        val latch = CountDownLatch(1)

        fun reducer(action: Int) = intent {
            reduce {
                threadName = Thread.currentThread().name
                state.copy(id = action)
            }
        }

        fun transformer(action: Int) = intent {
            threadName = Thread.currentThread().name
            val newEvent = action + 5

            reduce {
                state.copy(id = newEvent)
            }
        }
    }
}
