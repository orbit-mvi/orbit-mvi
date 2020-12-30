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

package org.orbitmvi.orbit.rxjava2

import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.strict.orbit
import org.orbitmvi.orbit.syntax.strict.reduce
import org.orbitmvi.orbit.test
import org.orbitmvi.orbit.test.ScopedBlockingWorkSimulator
import io.kotest.matchers.collections.shouldContainExactly
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

@ExperimentalCoroutinesApi
internal class RxJava2DslPluginDslThreadingTest {
    private val scope = TestCoroutineScope(Job())

    @AfterEach
    fun afterEach() {
        scope.cancel()
        scope.cleanupTestCoroutines()
    }

    @Test
    fun `blocking single does not block the container from receiving further intents`() {
        `blocking dsl function does not block the container from receiving further intents`(
            call = { blockingSingle() },
            mutex = { singleMutex }
        )
    }

    @Test
    fun `blocking single does not block the reducer`() {
        `blocking dsl function does not block the reducer`(
            call = { blockingSingle() },
            mutex = { singleMutex }
        )
    }

    @Test
    fun `blocking maybe does not block the container from receiving further intents`() {
        `blocking dsl function does not block the container from receiving further intents`(
            call = { blockingMaybe() },
            mutex = { maybeMutex }
        )
    }

    @Test
    fun `blocking maybe does not block the reducer`() {
        `blocking dsl function does not block the reducer`(
            call = { blockingMaybe() },
            mutex = { maybeMutex }
        )
    }

    @Test
    fun `blocking completable does not block the container from receiving further intents`() {
        `blocking dsl function does not block the container from receiving further intents`(
            call = { blockingCompletable() },
            mutex = { completableMutex }
        )
    }

    @Test
    fun `blocking completable does not block the reducer`() {
        `blocking dsl function does not block the reducer`(
            call = { blockingCompletable() },
            mutex = { completableMutex }
        )
    }

    @Test
    fun `blocking observable does not block the container from receiving further intents`() {
        `blocking dsl function does not block the container from receiving further intents`(
            call = { blockingObservable() },
            mutex = { observableMutex }
        )
    }

    @Test
    fun `blocking observable does not block the reducer`() {
        `blocking dsl function does not block the reducer`(
            call = { blockingObservable() },
            mutex = { observableMutex }
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
        middleware.single(action)

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

    private inner class Middleware : ContainerHost<TestState, String> {

        @Suppress("EXPERIMENTAL_API_USAGE")
        override val container = scope.container<TestState, String>(TestState(42))

        val singleMutex = Mutex(locked = true)
        val maybeMutex = Mutex(locked = true)
        val completableMutex = Mutex(locked = true)
        val observableMutex = Mutex(locked = true)
        val workSimulator = ScopedBlockingWorkSimulator(scope)

        fun reducer(action: Int) = orbit {
            reduce {
                state.copy(id = action)
            }
        }

        fun single(action: Int) = orbit {
            transformRx2Single {
                Single.just(action + 5)
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun blockingSingle() = orbit {
            transformRx2Single {
                Single.fromCallable {
                    singleMutex.unlock()
                    workSimulator.simulateWork()
                    1
                }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun blockingMaybe() = orbit {
            transformRx2Maybe {
                Maybe.fromCallable {
                    maybeMutex.unlock()
                    workSimulator.simulateWork()
                    1
                }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun blockingCompletable() = orbit {
            transformRx2Completable {
                Completable.fromCallable {
                    completableMutex.unlock()
                    workSimulator.simulateWork()
                    1
                }
            }
                .reduce {
                    state.copy(id = 123)
                }
        }

        fun blockingObservable() = orbit {
            transformRx2Observable {
                Observable.fromCallable {
                    observableMutex.unlock()
                    workSimulator.simulateWork()
                    1
                }
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}