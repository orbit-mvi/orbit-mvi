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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CoroutinePluginBehaviourTest {
    private val fixture = kotlinFixture()
    private val initialState = fixture<TestState>()

    @Nested
    inner class DslBehaviourTests {

        @BeforeEach
        fun beforeEach() {
            Orbit.registerDslPlugins(CoroutinePlugin)
        }

        @AfterEach
        fun afterEach() {
            Orbit.resetPlugins()
        }

        @Test
        fun `suspend transformation maps`() {
            val action = fixture<Int>()

            Middleware()
                .given(initialState)
                .whenever {
                    suspend(action)
                }
                .then {
                    states(
                        { TestState(action + 5) }
                    )
                }
        }

        @Test
        fun `flow transformation flatmaps`() {
            val action = fixture<Int>()

            Middleware()
                .given(initialState)
                .whenever {
                    flow(action)
                }
                .then {
                    states(
                        { TestState(action) },
                        { TestState(action + 1) },
                        { TestState(action + 2) },
                        { TestState(action + 3) }
                    )
                }
        }
    }

    @Nested
    inner class PluginRegistrationTests {

        @Test
        fun `suspend transformation crashes if the plugin is not included`() {
            val action = fixture<Int>()

            assertThrows<IllegalStateException> {
                Middleware()
                    .given(initialState)
                    .whenever {
                        flow(action)
                    }
                    .then {}
            }
        }

        @Test
        fun `flow transformation crashes if the plugin is not included`() {
            val action = fixture<Int>()

            assertThrows<IllegalStateException> {

                Middleware()
                    .given(initialState)
                    .whenever {
                        flow(action)
                    }
                    .then {}
            }
        }
    }

    private data class TestState(val id: Int)

    private class Middleware : Host<TestState, String> {
        override val container = Container.create<TestState, String>(TestState(42))

        fun suspend(action: Int) = orbit {
            transformSuspend {
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
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
