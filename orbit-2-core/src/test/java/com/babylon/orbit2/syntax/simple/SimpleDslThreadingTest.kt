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

import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.test
import com.babylon.orbit2.test.ScopedBlockingWorkSimulator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class SimpleDslThreadingTest {

    private val scope = TestCoroutineScope(Job())
    private val middleware = BaseDslMiddleware()

    @AfterTest
    fun afterTest() {
        scope.cancel()
        scope.cleanupTestCoroutines()
    }

    @Test
    fun `blocking intent with context switch does not block the reducer`() {
        val action = Random.nextInt()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.backgroundIntent()
        runBlocking {
            withTimeout(1000L) {
                middleware.intentMutex.withLock {}
            }
        }

        middleware.reducer(action)

        testFlowObserver.awaitCount(2)
        testFlowObserver.values.shouldContainExactly(TestState(42), TestState(action))
    }

    @Test
    fun `suspending intent does not block the reducer`() {
        val action = Random.nextInt()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.suspendingIntent()
        runBlocking {
            withTimeout(1000L) {
                middleware.intentMutex.withLock {}
            }
        }

        middleware.reducer(action)

        testFlowObserver.awaitCount(2)
        testFlowObserver.values.shouldContainExactly(TestState(42), TestState(action))
    }

    @Test
    fun `blocking intent without context switch blocks the reducer`() {
        val action = Random.nextInt()
        val middleware = BaseDslMiddleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.blockingIntent()

        runBlocking {
            withTimeout(1000L) {
                middleware.intentMutex.withLock {
                }
            }
        }

        middleware.reducer(action)

        testFlowObserver.awaitCount(2, 100L)
        testFlowObserver.values.shouldContainExactly(TestState(42))
    }

    @Test
    fun `blocking reducer blocks an intent`() {
        val middleware = BaseDslMiddleware()
        middleware.container.stateFlow.test()

        middleware.blockingReducer()
        runBlocking {
            withTimeout(1000L) {
                middleware.reducerMutex.withLock {}
            }
        }

        middleware.simpleIntent()

        shouldThrow<TimeoutCancellationException> {
            runBlocking {
                withTimeout(500L) {
                    middleware.intentMutex.withLock {}
                }
            }
        }
    }

    private data class TestState(val id: Int)

    private inner class BaseDslMiddleware : ContainerHost<TestState, String> {

        @Suppress("EXPERIMENTAL_API_USAGE")
        override val container = scope.container<TestState, String>(TestState(42))

        val intentMutex = Mutex(locked = true)
        val reducerMutex = Mutex(locked = true)
        val workSimulator = ScopedBlockingWorkSimulator(scope)

        fun reducer(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }

        fun blockingReducer() = intent {
            reduce {
                reducerMutex.unlock()
                workSimulator.simulateWork()
                state.copy(id = 123)
            }
        }

        fun backgroundIntent() = intent {
            intentMutex.unlock()
            withContext(Dispatchers.Default) {
                while (true) {
                    yield()
                }
            }
        }

        fun blockingIntent() = intent {
            intentMutex.unlock()
            while (true) {
                yield()
            }
        }

        fun suspendingIntent() = intent {
            intentMutex.unlock()
            delay(Int.MAX_VALUE.toLong())
        }

        fun simpleIntent() = intent {
            intentMutex.unlock()
        }
    }
}
