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

package org.orbitmvi.orbit.syntax.strict

import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test
import org.orbitmvi.orbit.test.ScopedBlockingWorkSimulator
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class BaseDslPluginThreadingTest {

    private val scope = TestCoroutineScope(Job())
    private val middleware = Middleware()

    @AfterTest
    fun afterTest() {
        scope.cancel()
        scope.cleanupTestCoroutines()
    }

    @Test
    fun `blocking transformer does not block the container from receiving further intents`() {
        val action = Random.nextInt()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.blockingTransformer()
        runBlocking {
            withTimeout(1000L) {
                middleware.transformerMutex.withLock { }
                delay(50)
            }
        }
        middleware.transformer(action)

        testFlowObserver.awaitCount(2)
        testFlowObserver.values.shouldContainExactly(TestState(42), TestState(action + 5))
    }

    @Test
    fun `blocking transformer does not block the reducer`() {
        val action = Random.nextInt()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.blockingTransformer()
        runBlocking {
            withTimeout(1000L) {
                middleware.transformerMutex.withLock { }
                delay(20)
            }
        }

        middleware.reducer(action)

        testFlowObserver.awaitCount(2)
        testFlowObserver.values.shouldContainExactly(TestState(42), TestState(action))
    }

    @Test
    fun `blocking side effect blocks the container from receiving further intents`() {
        `blocking dsl function blocks the container from receiving further intents`(
            call = { blockingSideEffect() },
            mutex = { sideEffectMutex }
        )
    }

    @Test
    fun `blocking side effect blocks the reducer`() {
        `blocking dsl function blocks the reducer`(
            call = { blockingSideEffect() },
            mutex = { sideEffectMutex }
        )
    }

    @Test
    fun `blocking reducer blocks the container from receiving further intents`() {
        `blocking dsl function blocks the container from receiving further intents`(
            call = { blockingReducer() },
            mutex = { reducerMutex }
        )
    }

    @Test
    fun `blocking reducer blocks further reductions`() {
        `blocking dsl function blocks the reducer`(
            call = { blockingReducer() },
            mutex = { reducerMutex }
        )
    }

    private fun `blocking dsl function blocks the container from receiving further intents`(
        call: Middleware.() -> Unit,
        mutex: Middleware.() -> Mutex
    ) {
        val action = Random.nextInt()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.call()
        runBlocking {
            withTimeout(1000L) {
                middleware.mutex().withLock { }
                delay(20)
            }
        }
        middleware.transformer(action)

        testFlowObserver.awaitCount(2, 100L)
        testFlowObserver.values.shouldContainExactly(
            TestState(42),
        )
    }

    private fun `blocking dsl function blocks the reducer`(
        call: Middleware.() -> Unit,
        mutex: Middleware.() -> Mutex
    ) {
        val action = Random.nextInt()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.call()
        runBlocking {
            withTimeout(1000L) {
                middleware.mutex().withLock { }
                delay(20)
            }
        }

        middleware.reducer(action)

        testFlowObserver.awaitCount(2, 100L)
        testFlowObserver.values.shouldContainExactly(TestState(42))
    }

    private data class TestState(val id: Int)

    private inner class Middleware : ContainerHost<TestState, String> {

        @Suppress("EXPERIMENTAL_API_USAGE")
        override val container = scope.container<TestState, String>(TestState(42))

        val reducerMutex = Mutex(locked = true)
        val transformerMutex = Mutex(locked = true)
        val sideEffectMutex = Mutex(locked = true)
        val workSimulator = ScopedBlockingWorkSimulator(scope)

        fun blockingReducer() = orbit {
            reduce {
                reducerMutex.unlock()
                workSimulator.simulateWork()
                state.copy(id = 123)
            }
        }

        fun reducer(action: Int) = orbit {
            reduce {
                state.copy(id = action)
            }
        }

        fun transformer(action: Int) = orbit {
            transform {
                action + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun blockingTransformer() = orbit {
            transform {
                transformerMutex.unlock()
                workSimulator.simulateWork()
                1
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun blockingSideEffect() = orbit {
            sideEffect {
                sideEffectMutex.unlock()
                workSimulator.simulateWork()
            }
        }
    }
}
