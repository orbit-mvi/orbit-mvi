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

package com.babylon.orbit2.rxjava3

import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.internal.RealContainer
import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.syntax.strict.reduce
import com.babylon.orbit2.test
import io.kotest.matchers.string.shouldStartWith
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import kotlin.random.Random

internal class RxJava3DslPluginDslThreadingTest {

    companion object {
        const val BACKGROUND_THREAD_PREFIX = "IO"
    }

    @Test
    fun `single transformation runs on IO dispatcher`() {
        val action = Random.nextInt()

        val middleware = Middleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.single(action)

        testFlowObserver.awaitCount(2)
        middleware.threadName.shouldStartWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `non empty maybe transformation runs on IO dispatcher`() {
        val action = Random.nextInt()

        val middleware = Middleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.maybe(action)

        testFlowObserver.awaitCount(2)
        middleware.threadName.shouldStartWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `empty maybe transformation runs on IO dispatcher`() {
        val action = Random.nextInt()

        val middleware = Middleware()

        middleware.maybeNot(action)

        middleware.latch.await()
        middleware.threadName.shouldStartWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `completable transformation runs on IO dispatcher`() {
        val action = Random.nextInt()

        val middleware = Middleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.completable(action)

        testFlowObserver.awaitCount(2)
        middleware.threadName.shouldStartWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `observable transformation runs on IO dispatcher`() {
        val action = Random.nextInt()

        val middleware = Middleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.observable(action)

        testFlowObserver.awaitCount(5)
        middleware.threadName.shouldStartWith(BACKGROUND_THREAD_PREFIX)
    }

    private data class TestState(val id: Int)

    private class Middleware : ContainerHost<TestState, String> {

        @Suppress("EXPERIMENTAL_API_USAGE")
        override val container = RealContainer<TestState, String>(
            initialState = TestState(42),
            parentScope = CoroutineScope(Dispatchers.Unconfined),
            settings = Container.Settings(
                backgroundDispatcher = newSingleThreadContext(BACKGROUND_THREAD_PREFIX)
            )
        )
        lateinit var threadName: String
        val latch = CountDownLatch(1)

        fun single(action: Int) = orbit {
            transformRx3Single {
                Single.just(action + 5)
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybe(action: Int) = orbit {
            transformRx3Maybe {
                Maybe.just(action + 5)
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybeNot(action: Int) = orbit {
            transformRx3Maybe {
                Maybe.empty<Int>()
                    .doOnSubscribe { threadName = Thread.currentThread().name }
                    .doOnSubscribe { latch.countDown() }
            }
                .reduce {
                    state.copy(id = action)
                }
        }

        fun completable(action: Int) = orbit {
            transformRx3Completable {
                Completable.complete()
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = action)
                }
        }

        fun observable(action: Int) = orbit {
            transformRx3Observable {
                Observable.just(action, action + 1, action + 2, action + 3)
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
