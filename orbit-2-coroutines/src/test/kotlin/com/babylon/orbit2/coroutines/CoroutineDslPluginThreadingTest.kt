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

package com.babylon.orbit2.coroutines

import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.syntax.strict.reduce
import com.babylon.orbit2.test
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

@ExperimentalCoroutinesApi
internal class CoroutineDslPluginThreadingTest {
    private val scope = TestCoroutineScope(Job())

    @AfterEach
    fun afterEach() {
        scope.cleanupTestCoroutines()
        scope.cancel()
    }

    @Test
    fun `blocking suspend does not block the container from receiving further intents`() {
        `blocking dsl function does not block the container from receiving further intents`(
            call = { blockingSuspend() },
            mutex = { suspendMutex }
        )
    }

    @Test
    fun `blocking suspend does not block the reducer`() {
        `blocking dsl function does not block the reducer`(
            call = { blockingSuspend() },
            mutex = { suspendMutex }
        )
    }

    @Test
    fun `blocking flow does not block the container from receiving further intents`() {
        `blocking dsl function does not block the container from receiving further intents`(
            call = { blockingFlow() },
            mutex = { flowMutex }
        )
    }

    @Test
    fun `blocking flow does not block the reducer`() {
        `blocking dsl function does not block the reducer`(
            call = { blockingFlow() },
            mutex = { flowMutex }
        )
    }

    private fun `blocking dsl function does not block the container from receiving further intents`(
        call: Middleware.() -> Unit,
        mutex: Middleware.() -> Mutex
    ) {
        val action = Random.nextInt()
        val middleware = Middleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.call()
        runBlocking {
            withTimeout(1000L) {
                middleware.mutex().withLock { }
                delay(20)
            }
        }
        middleware.suspend(action)

        testFlowObserver.awaitCount(2)
        testFlowObserver.values.shouldContainExactly(
            TestState(42),
            TestState(action + 5)
        )
    }

    private fun `blocking dsl function does not block the reducer`(
        call: Middleware.() -> Unit,
        mutex: Middleware.() -> Mutex
    ) {
        val action = Random.nextInt()
        val middleware = Middleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.call()
        runBlocking {
            withTimeout(1000L) {
                middleware.mutex().withLock { }
                delay(20)
            }
        }

        middleware.reducer(action)

        testFlowObserver.awaitCount(2)
        testFlowObserver.values.shouldContainExactly(TestState(42), TestState(action))
    }

    private data class TestState(val id: Int)

    @Suppress("UNREACHABLE_CODE", "ControlFlowWithEmptyBody", "EmptyWhileBlock")
    private inner class Middleware : ContainerHost<TestState, String> {

        @Suppress("EXPERIMENTAL_API_USAGE")
        override var container = scope.container<TestState, String>(TestState(42))
        val suspendMutex = Mutex(locked = true)
        val flowMutex = Mutex(locked = true)

        fun reducer(action: Int) = orbit {
            reduce {
                state.copy(id = action)
            }
        }

        fun suspend(action: Int) = orbit {
            transformSuspend {
                delay(50)
                action + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun blockingSuspend() = orbit {
            transformSuspend {
                suspendMutex.unlock()
                while (currentCoroutineContext().isActive) {
                }
                1
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun blockingFlow() = orbit {
            transformFlow {
                kotlinx.coroutines.flow.flow {
                    flowMutex.unlock()
                    while (currentCoroutineContext().isActive) {
                    }
                    emit(1)
                }
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
