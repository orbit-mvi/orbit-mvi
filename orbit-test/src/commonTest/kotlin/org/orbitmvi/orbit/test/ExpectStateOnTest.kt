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

package org.orbitmvi.orbit.test

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExpectStateOnTest {

    private val initialState = State.Loading

    @Test
    fun succeeds_if_initial_state_matches_expected_state() = runTest {
        StateTestMiddleware(this).test(this, settings = TestSettings(autoCheckInitialState = false)) {
            expectStateOn<State.Loading> { initialState }
        }
    }

    @Test
    fun fails_if_initial_state_does_not_match_type() = runTest {
        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(autoCheckInitialState = false)) {
                expectStateOn<State.Ready> { initialState }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "Expected value to be of type", ignoreCase = true) == true }
        }
    }

    @Test
    fun fails_if_initial_state_does_not_match_expected_state() = runTest {
        val someRandomState = State.Ready(Random.nextInt())

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(autoCheckInitialState = false)) {
                expectStateOn<State.Loading> { someRandomState }
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
            containerHost.newCount(action)
            containerHost.newCount(action2)
            expectStateOn<State.Loading> { State.Ready(count = action) }
            expectStateOn<State.Ready> { copy(count = action2) }
        }
    }

    @Test
    fun fails_if_more_states_emitted_than_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                expectStateOn<State.Loading> { State.Ready(count = action) }
                expectStateOn<State.Ready> { copy(count = action2) }
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
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectStateOn<State.Loading> { State.Ready(count = action) }
                expectStateOn<State.Ready> { copy(count = action2) }
                expectStateOn<State.Ready> { copy(count = action3) }
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
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectStateOn<State.Loading> { State.Ready(count = action2) }
                expectStateOn<State.Ready> { copy(count = action3) }
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
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectStateOn<State.Loading> { State.Ready(count = action) }
                expectStateOn<State.Ready> { copy(count = action3) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun fails_if_first_emitted_state_after_initial_does_not_match_type() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectStateOn<State.Ready> { State.Ready(count = action2) }
                expectStateOn<State.Ready> { copy(count = action3) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "Expected value to be of type", ignoreCase = true) == true }
        }
    }

    @Test
    fun fails_if_second_emitted_state_after_initial_does_not_match_type() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectStateOn<State.Loading> { State.Ready(count = action) }
                expectStateOn<State.Loading> { State.Ready(count = action3) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "Expected value to be of type", ignoreCase = true) == true }
        }
    }

    @Test
    fun fails_if_expected_states_are_out_of_order() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                expectStateOn<State.Loading> { State.Ready(count = action) }
                expectStateOn<State.Ready> { copy(count = action3) }
                expectStateOn<State.Ready> { copy(count = action2) }
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

                expectStateOn<State.Loading> { State.Ready(count = sideEffect) }
                awaitSideEffect()
            }
        }.also {
            assertTrue { it.message?.startsWith("Expected State but got SideEffectItem") == true }
        }
    }

    private inner class StateTestMiddleware(scope: TestScope) :
        ContainerHost<State, Int> {
        override val container = scope.backgroundScope.container<State, Int>(initialState)

        fun newCount(action: Int) = intent {
            reduce {
                State.Ready(count = action)
            }
        }

        fun newSideEffect(action: Int) = intent {
            postSideEffect(action)
        }
    }

    private sealed interface State {
        data object Loading : State

        data class Ready(val count: Int) : State
    }
}
