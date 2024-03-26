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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test.IgnoreIos
import org.orbitmvi.orbit.test.ScopedBlockingWorkSimulator
import org.orbitmvi.orbit.test.assertContainExactly
import org.orbitmvi.orbit.test.runBlocking
import org.orbitmvi.orbit.testFlowObserver
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
internal class SimpleDslThreadingTest {

    private val scope = CoroutineScope(Job())
    private val middleware = BaseDslMiddleware()

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    @IgnoreIos
    fun `blocking intent with context switch does not block the reducer`() = runBlocking {
        val action = Random.nextInt()
        val testFlowObserver = middleware.container.stateFlow.testFlowObserver()

        middleware.backgroundIntent()

        withTimeout(TIMEOUT) {
            middleware.intentMutex.withLock {}
        }

        middleware.reducer(action)

        testFlowObserver.awaitCount(2)
        testFlowObserver.values.assertContainExactly(TestState(42), TestState(action))
    }

    @Test
    fun `suspending intent does not block the reducer`() = runBlocking {
        val action = Random.nextInt()
        val testFlowObserver = middleware.container.stateFlow.testFlowObserver()

        middleware.suspendingIntent()
        withTimeout(TIMEOUT) {
            middleware.intentMutex.withLock {}
        }

        middleware.reducer(action)

        testFlowObserver.awaitCount(2)
        testFlowObserver.values.assertContainExactly(TestState(42), TestState(action))
    }

    @Test
    fun `blocking intent without context switch blocks the reducer`() = runBlocking {
        val action = Random.nextInt()
        val testFlowObserver = middleware.container.stateFlow.testFlowObserver()

        middleware.blockingIntent()

        withTimeout(TIMEOUT) {
            middleware.intentMutex.withLock {
            }
        }

        middleware.reducer(action)

        testFlowObserver.awaitCount(2, 100L)
        testFlowObserver.values.assertContainExactly(TestState(42))
    }

    @Test
    fun `blocking reducer blocks an intent`(): Unit = runBlocking {
        middleware.container.stateFlow.testFlowObserver()

        middleware.blockingReducer()
        withTimeout(TIMEOUT) {
            middleware.reducerMutex.withLock {}
        }

        middleware.simpleIntent()

        assertFailsWith<TimeoutCancellationException> {
            runBlocking {
                withTimeout(500L) {
                    middleware.intentMutex.withLock {}
                }
            }
        }
    }

    private data class TestState(val id: Int)

    @Suppress("ControlFlowWithEmptyBody", "EmptyWhileBlock")
    private inner class BaseDslMiddleware : ContainerHost<TestState, String> {

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
                while (currentCoroutineContext().isActive) {
                }
            }
        }

        fun blockingIntent() = intent {
            intentMutex.unlock()
            while (currentCoroutineContext().isActive) {
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

    companion object {
        private const val TIMEOUT = 1000L
    }
}
