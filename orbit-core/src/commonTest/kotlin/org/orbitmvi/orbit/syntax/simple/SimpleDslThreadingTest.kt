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

package org.orbitmvi.orbit.syntax.simple

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.IgnoreJs
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test.ScopedBlockingWorkSimulator
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class SimpleDslThreadingTest {

    @Test
    @IgnoreJs
    fun blocking_intent_with_context_switch_does_not_block_the_reducer() = runTest {
        val middleware = BaseDslMiddleware(this)
        val action = Random.nextInt()
        middleware.container.stateFlow.test {
            assertEquals(TestState(42), awaitItem())

            middleware.backgroundIntent()
            middleware.reducer(action)

            assertEquals(TestState(action), awaitItem())
            cancel()
        }
    }

    @Test
    fun suspending_intent_does_not_block_the_reducer() = runTest {
        val middleware = BaseDslMiddleware(this)
        val action = Random.nextInt()
        middleware.container.stateFlow.test {
            assertEquals(TestState(42), awaitItem())

            middleware.suspendingIntent()
            middleware.reducer(action)

            assertEquals(TestState(action), awaitItem())
        }
    }

    @Test
    @IgnoreJs
    fun blocking_intent_without_context_switch_blocks_the_reducer() = runTest {
        val middleware = BaseDslMiddleware(this)
        val action = Random.nextInt()
        middleware.container.stateFlow.test {
            assertEquals(TestState(42), awaitItem())

            middleware.blockingIntent()
            middleware.reducer(action)

            assertFails {
                awaitItem()
            }
        }
    }

    @Test
    @IgnoreJs
    fun blocking_reducer_blocks_an_intent() = runTest {
        val middleware = BaseDslMiddleware(this)
        val action = Random.nextInt()
        middleware.container.stateFlow.test {
            assertEquals(TestState(42), awaitItem())

            middleware.blockingReducer()
            middleware.reducer(action)

            assertFails {
                awaitItem()
            }
        }
    }

    private data class TestState(val id: Int)

    @Suppress("ControlFlowWithEmptyBody", "EmptyWhileBlock")
    private inner class BaseDslMiddleware(scope: TestScope) : ContainerHost<TestState, String> {

        override val container = scope.backgroundScope.container<TestState, String>(TestState(42))

        val workSimulator = ScopedBlockingWorkSimulator(scope.backgroundScope)

        fun reducer(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }

        fun blockingReducer() = intent {
            reduce {
                workSimulator.simulateWork()
                state.copy(id = 123)
            }
        }

        fun backgroundIntent() = intent {
            withContext(Dispatchers.Default) {
                while (currentCoroutineContext().isActive) {
                }
            }
        }

        fun blockingIntent() = intent {
            while (currentCoroutineContext().isActive) {
            }
        }

        fun suspendingIntent() = intent {
            delay(Int.MAX_VALUE.toLong())
        }
    }
}
