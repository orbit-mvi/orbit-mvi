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

package org.orbitmvi.orbit.syntax.strict

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.idling.IdlingResource
import org.orbitmvi.orbit.test.assertEventually
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class BaseDslPluginIdlingTest {

    private val testIdlingResource = TestIdlingResource()
    private val scope = TestCoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cleanupTestCoroutines()
        scope.cancel()
    }

    @Test
    fun `idle when nothing running`() {
        runBlocking {
            scope.createContainerHost()
            delay(50)
        }

        testIdlingResource.isIdle().shouldBeTrue()
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
                    testIdlingResource.isIdle().shouldBeFalse()
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
                    testIdlingResource.isIdle().shouldBeTrue()
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
                    testIdlingResource.isIdle().shouldBeTrue()
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
                    testIdlingResource.isIdle().shouldBeFalse()
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
                    testIdlingResource.isIdle().shouldBeTrue()
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
                    testIdlingResource.isIdle().shouldBeTrue()
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
                    testIdlingResource.isIdle().shouldBeFalse()
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
                    testIdlingResource.isIdle().shouldBeTrue()
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
                    testIdlingResource.isIdle().shouldBeTrue()
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
