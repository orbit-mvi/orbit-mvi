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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

internal class BaseDslThreadingTest {

    companion object {
        const val ORBIT_THREAD_PREFIX = "orbit"
        const val BACKGROUND_THREAD_PREFIX = "IO"
    }

    private val fixture = kotlinFixture()

    @Test
    fun `reducer executes on orbit dispatcher`() {
        val action = fixture<Int>()
        val middleware = BaseDslMiddleware()
        val testStreamObserver = middleware.container.stateStream.test()

        middleware.reducer(action)

        testStreamObserver.awaitCount(2)
        assertThat(middleware.threadName).startsWith(ORBIT_THREAD_PREFIX)
    }

    @Test
    fun `transformer executes on background dispatcher`() {
        val action = fixture<Int>()
        val middleware = BaseDslMiddleware()
        val testStreamObserver = middleware.container.stateStream.test()

        middleware.transformer(action)

        testStreamObserver.awaitCount(2)
        assertThat(middleware.threadName).startsWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `posting side effects executes on orbit dispatcher`() {
        val action = fixture<Int>()
        val middleware = BaseDslMiddleware()
        val testStreamObserver = middleware.container.sideEffectStream.test()

        middleware.postingSideEffect(action)

        testStreamObserver.awaitCount(1)
        assertThat(middleware.threadName).startsWith(ORBIT_THREAD_PREFIX)
    }

    @Test
    fun `side effect executes on orbit dispatcher`() {
        val action = fixture<Int>()
        val middleware = BaseDslMiddleware()

        middleware.sideEffect(action)

        middleware.latch.await()

        assertThat(middleware.threadName).startsWith(ORBIT_THREAD_PREFIX)
    }

    private data class TestState(val id: Int)

    private class BaseDslMiddleware : ContainerHost<TestState, String> {

        @Suppress("EXPERIMENTAL_API_USAGE")
        override val container = RealContainer<TestState, String>(
            initialState = TestState(42),
            settings = Container.Settings(),
            parentScope = CoroutineScope(Dispatchers.Unconfined),
            backgroundDispatcher = newSingleThreadContext(BACKGROUND_THREAD_PREFIX)
        )
        lateinit var threadName: String
        val latch = CountDownLatch(1)

        fun reducer(action: Int) = orbit {
            reduce {
                threadName = Thread.currentThread().name
                state.copy(id = action)
            }
        }

        fun transformer(action: Int) = orbit {
            transform {
                threadName = Thread.currentThread().name
                action + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun postingSideEffect(action: Int) = orbit {
            sideEffect {
                threadName = Thread.currentThread().name
                post(action.toString())
            }
        }

        fun sideEffect(action: Int) = orbit {
            sideEffect {
                threadName = Thread.currentThread().name
                latch.countDown()
                action.toString()
            }
        }
    }
}
