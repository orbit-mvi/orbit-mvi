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

import com.appmattus.kotlinfixture.kotlinFixture
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.asCoroutineDispatcher
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

internal class RxJava2PluginDslThreadingTest {
    private val fixture = kotlinFixture()

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            Orbit.registerDslPlugins(RxJava2Plugin)
        }
    }

    @Test
    fun `single transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.orbit.test()

        middleware.single(action)

        testStreamObserver.awaitCount(2)
        Assertions.assertThat(middleware.threadName).startsWith("IO")
    }

    @Test
    fun `non empty maybe transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.orbit.test()

        middleware.maybe(action)

        testStreamObserver.awaitCount(2)
        Assertions.assertThat(middleware.threadName).startsWith("IO")
    }

    @Test
    fun `empty maybe transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.orbit.test()

        middleware.maybeNot(action)

        middleware.latch.await()
        Assertions.assertThat(middleware.threadName).startsWith("IO")
    }

    @Test
    fun `completable transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.orbit.test()

        middleware.completable(action)

        testStreamObserver.awaitCount(2)
        Assertions.assertThat(middleware.threadName).startsWith("IO")
    }

    @Test
    fun `observable transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.orbit.test()

        middleware.observable(action)

        testStreamObserver.awaitCount(5)
        Assertions.assertThat(middleware.threadName).startsWith("IO")
    }

    private data class TestState(val id: Int)

    private class Middleware : Host<TestState, String> {
        override val container = RealContainer<TestState, String>(
            initialState = TestState(42),
            settings = Container.Settings(),
            backgroundDispatcher = Executors.newSingleThreadExecutor { Thread(it, "IO") }
                .asCoroutineDispatcher()
        )
        lateinit var threadName: String
        val latch = CountDownLatch(1)

        fun single(action: Int) = orbit(action) {
            transformRx2Single {
                Single.just(event + 5)
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybe(action: Int) = orbit(action) {
            transformRx2Maybe {
                Maybe.just(event + 5)
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybeNot(action: Int) = orbit(action) {
            transformRx2Maybe {
                Maybe.empty<Int>()
                    .doOnSubscribe { threadName = Thread.currentThread().name }
                    .doOnSubscribe { latch.countDown() }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun completable(action: Int) = orbit(action) {
            transformRx2Completable {
                Completable.complete()
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun observable(action: Int) = orbit(action) {
            transformRx2Observable {
                Observable.just(event, event + 1, event + 2, event + 3)
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
