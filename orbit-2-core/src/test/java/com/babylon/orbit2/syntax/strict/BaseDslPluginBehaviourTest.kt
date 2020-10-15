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

import com.appmattus.kotlinfixture.kotlinFixture
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.assert
import com.babylon.orbit2.container
import com.babylon.orbit2.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Test

internal class BaseDslPluginBehaviourTest {

    private val fixture = kotlinFixture()
    private val initialState = fixture<TestState>()

    @Test
    fun `reducer produces new states`() {
        val action = fixture<Int>()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.reducer(action)

        middleware.assert(initialState) {
            states(
                { TestState(action) }
            )
        }
    }

    @Test
    fun `transformer maps values`() {
        val action = fixture<Int>()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.transformer(action)

        middleware.assert(initialState) {
            states(
                { TestState(action + 5) }
            )
        }
    }

    @Test
    fun `posting side effects emit side effects`() {
        val action = fixture<Int>()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.postingSideEffect(action)

        middleware.assert(initialState) {
            postedSideEffects(action.toString())
        }
    }

    @Test
    fun `side effect does not post anything if post is not called`() {
        val action = fixture<Int>()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.sideEffect(action)

        middleware.assert(initialState) {}
    }

    @Test
    fun `allows nullable event`() {
        val action = null
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.allowsNulls(action)

        middleware.assert(initialState) {
            postedSideEffects(action.toString())
        }
    }

    private data class TestState(val id: Int)

    private class BaseDslMiddleware : ContainerHost<TestState, String> {
        override val container = CoroutineScope(Dispatchers.Unconfined).container<TestState, String>(
            TestState(42)
        )

        fun reducer(action: Int) = orbit {
            reduce {
                state.copy(id = action)
            }
        }

        fun transformer(action: Int) = orbit {
            transform {
                action + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun postingSideEffect(action: Int) = orbit {
            sideEffect {
                post(action.toString())
            }
        }

        fun sideEffect(action: Int) = orbit {
            sideEffect {
                action.toString()
            }
        }

        fun allowsNulls(action: Int?) = orbit {
            transform {
                action
            }.reduce {
                event?.let { event -> state.copy(id = event) } ?: state
            }.sideEffect {
                post(event.toString())
            }
        }
    }
}
