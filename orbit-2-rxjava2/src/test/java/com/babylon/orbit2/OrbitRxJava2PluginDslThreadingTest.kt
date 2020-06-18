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
import kotlinx.coroutines.newSingleThreadContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

internal class OrbitRxJava2PluginDslThreadingTest {

    companion object {
        const val BACKGROUND_THREAD_PREFIX = "IO"
    }

    private val fixture = kotlinFixture()

    @BeforeEach
    fun beforeEach() {
        Orbit.registerDslPlugins(OrbitRxJava2Plugin)
    }

    @AfterEach
    fun afterEach() {
        Orbit.resetPlugins()
    }

    @Test
    fun `single transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.stateStream.test()

        middleware.single(action)

        testStreamObserver.awaitCount(2)
        assertThat(middleware.threadName).startsWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `non empty maybe transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.stateStream.test()

        middleware.maybe(action)

        testStreamObserver.awaitCount(2)
        assertThat(middleware.threadName).startsWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `empty maybe transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()

        middleware.maybeNot(action)

        middleware.latch.await()
        assertThat(middleware.threadName).startsWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `completable transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.stateStream.test()

        middleware.completable(action)

        testStreamObserver.awaitCount(2)
        assertThat(middleware.threadName).startsWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `observable transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.stateStream.test()

        middleware.observable(action)

        testStreamObserver.awaitCount(5)
        assertThat(middleware.threadName).startsWith(BACKGROUND_THREAD_PREFIX)
    }

    private data class TestState(val id: Int)

    private class Middleware : Host<TestState, String> {
        @Suppress("EXPERIMENTAL_API_USAGE")
        override val container: Container<TestState, String> = RealContainer(
            initialState = TestState(42),
            settings = Container.Settings(),
            backgroundDispatcher = newSingleThreadContext(BACKGROUND_THREAD_PREFIX)
        )
        lateinit var threadName: String
        val latch = CountDownLatch(1)

        fun single(action: Int) = orbit {
            transformRx2Single {
                Single.just(action + 5)
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybe(action: Int) = orbit {
            transformRx2Maybe {
                Maybe.just(action + 5)
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybeNot(action: Int) = orbit {
            transformRx2Maybe {
                Maybe.empty<Int>()
                    .doOnSubscribe { threadName = Thread.currentThread().name }
                    .doOnSubscribe { latch.countDown() }
            }
                .reduce {
                    state.copy(id = action)
                }
        }

        fun completable(action: Int) = orbit {
            transformRx2Completable {
                Completable.complete()
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = action)
                }
        }

        fun observable(action: Int) = orbit {
            transformRx2Observable {
                Observable.just(action, action + 1, action + 2, action + 3)
                    .doOnSubscribe { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
