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

import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.internal.RealContainer
import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.syntax.strict.reduce
import com.babylon.orbit2.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.newSingleThreadContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class CoroutineDslPluginThreadingTest {

    companion object {
        const val BACKGROUND_THREAD_PREFIX = "IO"
    }

    @Test
    fun `suspend transformation runs on IO dispatcher`() {
        val action = Random.nextInt()

        val middleware = Middleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.suspend(action)

        testFlowObserver.awaitCount(2)
        assertThat(middleware.threadName).startsWith(BACKGROUND_THREAD_PREFIX)
    }

    @Test
    fun `flow transformation runs on IO dispatcher`() {
        val action = Random.nextInt()

        val middleware = Middleware()
        val testFlowObserver = middleware.container.stateFlow.test()

        middleware.flow(action)

        testFlowObserver.awaitCount(5)
        assertThat(middleware.threadName).startsWith(BACKGROUND_THREAD_PREFIX)
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

        fun suspend(action: Int) = orbit {
            transformSuspend {
                threadName = Thread.currentThread().name
                delay(50)
                action + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun flow(action: Int) = orbit {
            transformFlow {
                flowOf(action, action + 1, action + 2, action + 3)
                    .onEach { delay(50) }
                    .onEach { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
