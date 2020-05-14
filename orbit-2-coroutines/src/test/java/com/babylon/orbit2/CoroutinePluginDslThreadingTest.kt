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
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

internal class CoroutinePluginDslThreadingTest {
    private val fixture = kotlinFixture()

    @BeforeEach
    fun beforeEach() {
        Orbit.registerDslPlugins(CoroutinePlugin)
    }

    @AfterEach
    fun afterEach() {
        Orbit.resetPlugins()
    }

    @Test
    fun `suspend transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.orbit.test()

        middleware.suspend(action)

        testStreamObserver.awaitCount(2)
        assertThat(middleware.threadName).startsWith("IO")
    }

    @Test
    fun `flow transformation runs on IO dispatcher`() {
        val action = fixture<Int>()

        val middleware = Middleware()
        val testStreamObserver = middleware.container.orbit.test()

        middleware.flow(action)

        testStreamObserver.awaitCount(5)
        assertThat(middleware.threadName).startsWith("IO")
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

        fun suspend(action: Int) = orbit(action) {
            transformSuspend {
                threadName = Thread.currentThread().name
                delay(50)
                event + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun flow(action: Int) = orbit(action) {
            transformFlow {
                flowOf(event, event + 1, event + 2, event + 3)
                    .onEach { delay(50) }
                    .onEach { threadName = Thread.currentThread().name }
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
