/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SideEffectTest {

    private val initialState = State()

    @Test
    fun `succeeds if posted side effects match expected side effects`() = runTest {
        val sideEffects = List(Random.nextInt(1, 5)) { Random.nextInt() }

        SideEffectTestMiddleware(this).test(this) {
            sideEffects.forEach { invokeIntent { something(it) } }

            expectInitialState()

            assertEquals(
                sideEffects,
                buildList {
                    repeat(sideEffects.size) {
                        add(awaitSideEffect())
                    }
                }
            )
        }
    }

    @Test
    fun `succeeds if posted side effects match expected side effects - shorthand syntax`() = runTest {
        val sideEffects = List(Random.nextInt(1, 5)) { Random.nextInt() }

        SideEffectTestMiddleware(this).test(this) {
            sideEffects.forEach { invokeIntent { something(it) } }

            expectInitialState()

            sideEffects.forEach {
                expectSideEffect(it)
            }
        }
    }

    @Test
    fun `fails if posted side effects do not match expected side effects`() = runTest {
        val sideEffects = List(Random.nextInt(1, 5)) { Random.nextInt() }
        val sideEffects2 = List(Random.nextInt(1, 5)) { Random.nextInt() }

        assertFailsWith<AssertionError> {
            SideEffectTestMiddleware(this).test(this) {
                sideEffects.forEach { invokeIntent { something(it) } }

                expectInitialState()
                assertEquals(
                    sideEffects2,
                    buildList {
                        repeat(sideEffects.size) {
                            add(awaitSideEffect())
                        }
                    }
                )
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun `fails if expected a side effect but got a state`() = runTest {
        val sideEffect = Random.nextInt()

        assertFailsWith<AssertionError> {
            SideEffectTestMiddleware(this).test(this) {
                invokeIntent { newState(sideEffect) }
                invokeIntent { something(sideEffect) }

                expectInitialState()
                awaitSideEffect()
                awaitState()
            }
        }.also {
            assertTrue { it.message?.startsWith("Expected Side Effect but got StateItem") == true }
        }
    }

    private inner class SideEffectTestMiddleware(scope: TestScope) : ContainerHost<State, Int> {
        override val container = scope.backgroundScope.container<State, Int>(initialState)

        fun newState(count: Int) = intent {
            reduce { state.copy(count = count) }
        }

        fun something(action: Int) = intent {
            postSideEffect(action)
            somethingElse(action.toString())
        }

        fun somethingElse(action: String) = intent {
            println(action)
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
