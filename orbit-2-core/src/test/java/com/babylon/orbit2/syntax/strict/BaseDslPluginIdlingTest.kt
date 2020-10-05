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

package com.babylon.orbit2.syntax.strict

import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.idling.IdlingResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseDslPluginIdlingTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val testIdlingResource = TestIdlingResource()

    @AfterEach
    fun after() {
        scope.cancel()
    }

    @Test
    fun `idle when nothing running`() {
        runBlocking {
            scope.createContainerHost()
            delay(50)
        }

        assertTrue(testIdlingResource.isIdle())
    }

    @Test
    fun `transform not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transform {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertFalse(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transform idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transform(registerIdling = false) {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transform idle after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transform {
                    mutex.unlock()
                }
            }

            mutex.withLock {
                assertEventually {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `sideEffect not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                sideEffect {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertFalse(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `sideEffect idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                sideEffect(registerIdling = false) {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `sideEffect idle after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                sideEffect {
                    runBlocking {
                        mutex.unlock()
                    }
                }
            }

            mutex.withLock {
                assertEventually {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `reduce not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                reduce {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        state
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertFalse(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `reduce idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                reduce(registerIdling = false) {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        state
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `reduce idle after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                reduce {
                    runBlocking {
                        mutex.unlock()
                        state
                    }
                }
            }

            mutex.withLock {
                assertEventually {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    private suspend fun assertEventually(block: suspend () -> Unit) {
        withTimeout(TIMEOUT) {
            while (true) {
                try {
                    block()
                    break
                } catch (ignored: Throwable) {
                    yield()
                }
            }
        }
    }

    private fun CoroutineScope.createContainerHost(): ContainerHost<TestState, Int> {
        return object : ContainerHost<TestState, Int> {
            override val container: Container<TestState, Int> = container(
                initialState = TestState(0),
                settings = Container.Settings(idlingRegistry = testIdlingResource)

            )
        }
    }

    data class TestState(val value: Int)

    class TestIdlingResource : IdlingResource {
        private var counter = 0

        override fun increment() {
            counter++
        }

        override fun decrement() {
            counter--
        }

        override fun close() = Unit

        fun isIdle() = counter == 0
    }

    companion object {
        private const val TIMEOUT = 500L
    }
}
