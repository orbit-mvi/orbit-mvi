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
import org.junit.jupiter.api.Test

internal class BaseDslBehaviourTest {

    private val fixture = kotlinFixture()
    private val initialState = fixture<TestState>()

    @Test
    fun `reducer produces new states`() {
        val action = fixture<Int>()

        BaseDslMiddleware()
            .given(initialState)
            .whenever {
                reducer(action)
            }
            .then {
                states(
                    { TestState(action) }
                )
            }
    }

    @Test
    fun `transformer maps values`() {
        val action = fixture<Int>()

        BaseDslMiddleware()
            .given(initialState)
            .whenever {
                transformer(action)
            }
            .then {
                states(
                    { TestState(action + 5) }
                )
            }
    }

    @Test
    fun `posting side effects emit side effects`() {
        val action = fixture<Int>()

        BaseDslMiddleware()
            .given(initialState)
            .whenever {
                postingSideEffect(action)
            }
            .then {
                postedSideEffects(action.toString())
            }
    }

    @Test
    fun `side effect does not post anything if post is not called`() {
        val action = fixture<Int>()

        BaseDslMiddleware()
            .given(initialState)
            .whenever {
                sideEffect(action)
            }
            .then {}
    }

    private data class TestState(val id: Int)

    private class BaseDslMiddleware : Host<TestState, String> {
        override val container = Container.create<TestState, String>(
            TestState(42)
        )

        fun reducer(action: Int) = orbit(action) {
            reduce {
                state.copy(id = event)
            }
        }

        fun transformer(action: Int) = orbit(action) {
            transform {
                event + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun postingSideEffect(action: Int) = orbit(action) {
            sideEffect {
                post(event.toString())
            }
        }

        fun sideEffect(action: Int) = orbit(action) {
            sideEffect {
                event.toString()
            }
        }
    }
}
