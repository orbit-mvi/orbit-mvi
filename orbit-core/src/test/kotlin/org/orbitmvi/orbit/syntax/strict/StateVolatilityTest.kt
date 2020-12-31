/*
 * Copyright 2021 Mikolaj Leszczynski & Matthew Dolan
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

package org.orbitmvi.orbit.syntax.strict

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
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
internal class StateVolatilityTest {
    companion object {
        private const val TIMEOUT = 5000L
    }

    private val scope = TestCoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cleanupTestCoroutines()
        scope.cancel()
    }

    @Test
    fun `state is not volatile`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val stateChangedMutex = Mutex(locked = true)
            val completionMutex = Mutex(locked = true)

            containerHost.orbit {
                transform {
                    runBlocking {
                        val initialState = state

                        stateChangedMutex.withLock {
                            delay(10)
                            state.shouldBe(initialState)
                            completionMutex.unlock()
                        }
                    }
                }
            }

            containerHost.orbit {
                reduce {
                    runBlocking {
                        delay(50)
                        state.copy(value = state.value + 1).also {
                            stateChangedMutex.unlock()
                        }
                    }
                }
            }

            withTimeout(TIMEOUT) {
                completionMutex.withLock { }
            }
        }
    }

    @Test
    fun `volatile state changes mid-flow`() {
        runBlocking {
            val container = scope.createContainerHost()

            val stateChangedMutex = Mutex(locked = true)
            val completionMutex = Mutex(locked = true)

            container.orbit {
                transform {
                    runBlocking {
                        val initialState = volatileState()

                        stateChangedMutex.withLock {
                            delay(10)
                            volatileState().shouldNotBe(initialState)
                            completionMutex.unlock()
                        }
                    }
                }
            }

            container.orbit {
                reduce {
                    runBlocking {
                        delay(50)
                        state.copy(value = state.value + 1).also {
                            stateChangedMutex.unlock()
                        }
                    }
                }
            }

            withTimeout(TIMEOUT) {
                completionMutex.withLock { }
            }
        }
    }

    private fun CoroutineScope.createContainerHost(): ContainerHost<TestState, Int> {
        return object : ContainerHost<TestState, Int> {
            override val container: Container<TestState, Int> = container(
                initialState = TestState(0)
            )
        }
    }

    data class TestState(val value: Int)
}
