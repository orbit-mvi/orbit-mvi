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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ExperimentalCoroutinesApi
internal class StateVolatilityTest {
    companion object {
        private const val TIMEOUT = 5000L
    }

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
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
                            assertEquals(initialState, state)
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
                            delay(100)
                            println(initialState)
                            println(volatileState())
                            assertNotEquals(initialState, volatileState())
                            completionMutex.unlock()
                        }
                    }
                }
            }

            delay(50)

            container.orbit {
                reduce {
                    runBlocking {
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
