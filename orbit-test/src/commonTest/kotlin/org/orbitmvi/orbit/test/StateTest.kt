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

class StateTest {

    private val initialState = State()

    @Test
    fun succeeds_if_initial_state_matches_expected_state() = runTest {
        StateTestMiddleware(this).test(this) {
            expectInitialState()
        }
    }

    @Test
    fun fails_if_initial_state_does_not_match_expected_state() = runTest {
        val someRandomState = State()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                assertEquals(someRandomState, awaitState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun succeeds_if_emitted_states_match_expected_states() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        StateTestMiddleware(this).test(this) {
            expectInitialState()
            containerHost.newCount(action)
            containerHost.newCount(action2)
            assertEquals(State(count = action), awaitState())
            assertEquals(State(count = action2), awaitState())
        }
    }

    @Test
    fun fails_if_more_states_emitted_than_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                assertEquals(State(count = action), awaitState())
                assertEquals(State(count = action2), awaitState())
            }
        }.also {
            assertTrue { it.message?.startsWith("Unconsumed events found") == true }
        }
    }

    @Test
    fun fails_if_one_more_state_expected_than_emitted() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(State(count = action), awaitState())
                assertEquals(State(count = action2), awaitState())
                assertEquals(State(count = action3), awaitState())
            }
        }.also {
            assertTrue(it.message?.matches("No value produced in [0-9]+s".toRegex()) == true)
        }
    }

    @Test
    fun fails_if_first_emitted_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(State(count = action2), awaitState())
                assertEquals(State(count = action3), awaitState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun fails_if_second_emitted_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(State(count = action), awaitState())
                assertEquals(State(count = action3), awaitState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun fails_if_expected_states_are_out_of_order() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                assertEquals(State(count = action), awaitState())
                assertEquals(State(count = action3), awaitState())
                assertEquals(State(count = action2), awaitState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun fails_if_expected_a_state_but_got_a_side_effect() = runTest {
        val sideEffect = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                containerHost.newSideEffect(sideEffect)
                containerHost.newCount(sideEffect)

                expectInitialState()
                awaitState()
                awaitSideEffect()
            }
        }.also {
            assertTrue { it.message?.startsWith("Expected State but got SideEffectItem") == true }
        }
    }

    @Test
    fun can_assert_state_changes_only() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        StateTestMiddleware(this).test(this) {
            expectInitialState()
            containerHost.newList(action)
            containerHost.newList(action2)
            containerHost.newList(action3)
            expectState { copy(list = listOf(action)) }
            expectState { copy(list = listOf(action, action2)) }
            expectState { copy(list = listOf(action, action2, action3)) }
        }
    }

    @Test
    fun can_assert_state_changes_only__shorthand() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        StateTestMiddleware(this).test(this) {
            expectInitialState()
            containerHost.newList(action)
            containerHost.newList(action2)
            containerHost.newList(action3)
            expectState(State(count = initialState.count, list = listOf(action)))
            expectState(State(count = initialState.count, list = listOf(action, action2)))
            expectState(State(count = initialState.count, list = listOf(action, action2, action3)))
        }
    }

    @Test
    fun can_assert_state_changes_only__failure() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                containerHost.newList(action)
                containerHost.newList(action2)
                containerHost.newList(action3)
                expectState { copy(list = listOf(action)) }
                expectState { copy(list = listOf(action, action2, action3)) }
                expectState { copy(list = listOf(action, action2)) }
            }
        }
    }

    @Test
    fun can_assert_state_changes_only__shorthand_failure() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                containerHost.newList(action)
                containerHost.newList(action2)
                containerHost.newList(action3)
                expectState(State(count = initialState.count, list = listOf(action)))
                expectState(State(count = initialState.count, list = listOf(action, action2, action3)))
                expectState(State(count = initialState.count, list = listOf(action, action2)))
            }
        }
    }

    private inner class StateTestMiddleware(scope: TestScope) :
        ContainerHost<State, Int> {
        override val container = scope.backgroundScope.container<State, Int>(initialState)

        fun newCount(action: Int) = intent {
            reduce {
                State(count = action)
            }
        }

        fun newList(action: Int) = intent {
            reduce {
                state.copy(list = state.list + action)
            }
        }

        fun newSideEffect(action: Int) = intent {
            postSideEffect(action)
        }
    }

    private data class State(
        val count: Int = Random.nextInt(),
        val list: List<Int> = emptyList()
    )
}
