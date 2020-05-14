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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class CoroutinePluginDslBehaviourTest {
    private val fixture = kotlinFixture()
    private val initialState = fixture<TestState>()

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            Orbit.registerDslPlugins(CoroutinePlugin)
        }
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

    private data class TestState(val id: Int)

    private class Middleware : Host<TestState, String> {
        override val container = Container.create<TestState, String>(TestState(42))

        fun suspend(action: Int) = orbit(action) {
            transformSuspend {
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
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
