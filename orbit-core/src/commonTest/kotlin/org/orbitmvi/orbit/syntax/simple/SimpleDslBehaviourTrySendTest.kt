/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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
 */

package org.orbitmvi.orbit.syntax.simple

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.OrbitGlobalSettings
import org.orbitmvi.orbit.Send
import org.orbitmvi.orbit.TrySend
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test.test
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail

internal class SimpleDslBehaviourTrySendTest {

    private val initialState = TestState()

    @BeforeTest
    fun setUp() {
        OrbitGlobalSettings.orbitDispatch = TrySend(onFailure = {
            println("Failed! $it")
            fail(it)
        })
    }

    @AfterTest
    fun tearDown() {
        OrbitGlobalSettings.orbitDispatch = Send
    }

    @Test
    fun reducer_produces_new_states() = runTest {
        val action = Random.nextInt()
        BaseDslMiddleware(this).test(this, initialState) {
            containerHost.reducer(action)
            containerHost.reducer(action)
            containerHost.reducer(action)
            containerHost.reducer(action)


            containerHost.reducer(action)

            expectState { TestState(action) }
        }
    }

    @Test
    fun transformer_maps_values() = runTest {
        val action = Random.nextInt()
        BaseDslMiddleware(this).test(this, initialState) {
            containerHost.transformer(action)

            expectState { TestState(action + 5) }
        }
    }

    @Test
    fun posting_side_effects_emit_side_effects() = runTest {
        val action = Random.nextInt()
        BaseDslMiddleware(this).test(this, initialState) {
            containerHost.postingSideEffect(action)

            expectSideEffect(action.toString())
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class BaseDslMiddleware(scope: TestScope) : ContainerHost<TestState, String> {
        override val container = scope.backgroundScope.container<TestState, String>(TestState(42))

        fun reducer(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }

        fun transformer(action: Int) = intent {
            val newAction = action + dataSource()

            reduce {
                state.copy(id = newAction)
            }
        }

        fun postingSideEffect(action: Int) = intent {
            postSideEffect(action.toString())
        }

        private suspend fun dataSource(): Int {
            delay(100)
            return 5
        }
    }
}
