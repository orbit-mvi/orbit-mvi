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
import org.orbitmvi.orbit.ContainerHostWithExternalState
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.withExternalState
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class ExpectStateOnWithExternalStateTest {

    private val initialState = InternalState.Loading

    @Test
    fun internal_succeeds_if_initial_state_matches_expected_state() = runTest {
        StateTestMiddleware(this).testInternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
            expectInternalStateOn<InternalState.Loading> { initialState }
        }
    }

    @Test
    fun external_succeeds_if_initial_state_matches_expected_state() = runTest {
        StateTestMiddleware(this).testExternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
            expectExternalStateOn<ExternalState.Loading> { ExternalState.Loading }
        }
    }

    @Test
    fun internal_fails_if_initial_state_does_not_match_type() = runTest {
        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
                expectInternalStateOn<InternalState.Ready> { initialState }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "Expected value to be of type", ignoreCase = true) == true }
        }
    }

    @Test
    fun external_fails_if_initial_state_does_not_match_type() = runTest {
        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
                expectExternalStateOn<ExternalState.Ready> { ExternalState.Loading }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "Expected value to be of type", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_fails_if_initial_state_does_not_match_expected_state() = runTest {
        val someRandomState = InternalState.Ready(Random.nextInt())

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
                expectInternalStateOn<InternalState.Loading> { someRandomState }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun external_fails_if_initial_state_does_not_match_expected_state() = runTest {
        val someRandomState = ExternalState.Ready(Random.nextInt().toString())

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
                expectExternalStateOn<ExternalState.Loading> { someRandomState }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_succeeds_if_emitted_states_match_expected_states() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        StateTestMiddleware(this).testInternalState(this) {
            containerHost.newCount(action)
            containerHost.newCount(action2)
            expectInternalStateOn<InternalState.Loading> { InternalState.Ready(count = action) }
            expectInternalStateOn<InternalState.Ready> { copy(count = action2) }
        }
    }

    @Test
    fun external_succeeds_if_emitted_states_match_expected_states() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        StateTestMiddleware(this).testExternalState(this,) {
            containerHost.newCount(action)
            containerHost.newCount(action2)
            expectExternalStateOn<ExternalState.Loading> { ExternalState.Ready(count = action.toString()) }
            expectExternalStateOn<ExternalState.Ready> { copy(count = action2.toString()) }
        }
    }

    @Test
    fun internal_fails_if_more_states_emitted_than_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                expectInternalStateOn<InternalState.Loading> { InternalState.Ready(count = action) }
                expectInternalStateOn<InternalState.Ready> { copy(count = action2) }
            }
        }.also {
            assertTrue { it.message?.startsWith("Unconsumed events found") == true }
        }
    }

    @Test
    fun external_fails_if_more_states_emitted_than_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this,) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                expectExternalStateOn<ExternalState.Loading> { ExternalState.Ready(count = action.toString()) }
                expectExternalStateOn<ExternalState.Ready> { copy(count = action2.toString()) }
            }
        }.also {
            assertTrue { it.message?.startsWith("Unconsumed events found") == true }
        }
    }

    @Test
    fun internal_fails_if_one_more_state_expected_than_emitted() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this, timeout = 500.milliseconds) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectInternalStateOn<InternalState.Loading> { InternalState.Ready(count = action) }
                expectInternalStateOn<InternalState.Ready> { copy(count = action2) }
                expectInternalStateOn<InternalState.Ready> { copy(count = action3) }
            }
        }.also {
            assertTrue(it.message?.startsWith("No value produced in ") == true)
        }
    }

    @Test
    fun external_fails_if_one_more_state_expected_than_emitted() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this, timeout = 500.milliseconds,) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectExternalStateOn<ExternalState.Loading> { ExternalState.Ready(count = action.toString()) }
                expectExternalStateOn<ExternalState.Ready> { copy(count = action2.toString()) }
                expectExternalStateOn<ExternalState.Ready> { copy(count = action3.toString()) }
            }
        }.also {
            assertTrue(it.message?.startsWith("No value produced in ") == true)
        }
    }

    @Test
    fun internal_fails_if_first_emitted_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectInternalStateOn<InternalState.Loading> { InternalState.Ready(count = action2) }
                expectInternalStateOn<InternalState.Ready> { copy(count = action3) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun external_fails_if_first_emitted_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this,) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectExternalStateOn<ExternalState.Loading> { ExternalState.Ready(count = action2.toString()) }
                expectExternalStateOn<ExternalState.Ready> { copy(count = action3.toString()) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_fails_if_second_emitted_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectInternalStateOn<InternalState.Loading> { InternalState.Ready(count = action) }
                expectInternalStateOn<InternalState.Ready> { copy(count = action3) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun external_fails_if_second_emitted_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this,) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectExternalStateOn<ExternalState.Loading> { ExternalState.Ready(count = action.toString()) }
                expectExternalStateOn<ExternalState.Ready> { copy(count = action3.toString()) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_fails_if_first_emitted_state_after_initial_does_not_match_type() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectInternalStateOn<InternalState.Ready> { InternalState.Ready(count = action2) }
                expectInternalStateOn<InternalState.Ready> { copy(count = action3) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "Expected value to be of type", ignoreCase = true) == true }
        }
    }

    @Test
    fun external_fails_if_first_emitted_state_after_initial_does_not_match_type() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this,) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectExternalStateOn<ExternalState.Ready> { ExternalState.Ready(count = action2.toString()) }
                expectExternalStateOn<ExternalState.Ready> { copy(count = action3.toString()) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "Expected value to be of type", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_fails_if_second_emitted_state_after_initial_does_not_match_type() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectInternalStateOn<InternalState.Loading> { InternalState.Ready(count = action) }
                expectInternalStateOn<InternalState.Loading> { InternalState.Ready(count = action3) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "Expected value to be of type", ignoreCase = true) == true }
        }
    }

    @Test
    fun external_fails_if_second_emitted_state_after_initial_does_not_match_type() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this,) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                expectExternalStateOn<ExternalState.Loading> { ExternalState.Ready(count = action.toString()) }
                expectExternalStateOn<ExternalState.Loading> { ExternalState.Ready(count = action3.toString()) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "Expected value to be of type", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_fails_if_expected_states_are_out_of_order() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                expectInternalStateOn<InternalState.Loading> { InternalState.Ready(count = action) }
                expectInternalStateOn<InternalState.Ready> { copy(count = action3) }
                expectInternalStateOn<InternalState.Ready> { copy(count = action2) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun external_fails_if_expected_states_are_out_of_order() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this,) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                expectExternalStateOn<ExternalState.Loading> { ExternalState.Ready(count = action.toString()) }
                expectExternalStateOn<ExternalState.Ready> { copy(count = action3.toString()) }
                expectExternalStateOn<ExternalState.Ready> { copy(count = action2.toString()) }
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_fails_if_expected_a_state_but_got_a_side_effect() = runTest {
        val sideEffect = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testInternalState(this) {
                containerHost.newSideEffect(sideEffect)
                containerHost.newCount(sideEffect)

                expectInternalStateOn<InternalState.Loading> { InternalState.Ready(count = sideEffect) }
                awaitSideEffect()
            }
        }.also {
            assertTrue { it.message?.startsWith("Expected Internal State but got SideEffectItem") == true }
        }
    }

    @Test
    fun external_fails_if_expected_a_state_but_got_a_side_effect() = runTest {
        val sideEffect = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).testExternalState(this,) {
                containerHost.newSideEffect(sideEffect)
                containerHost.newCount(sideEffect)

                expectExternalStateOn<ExternalState.Loading> { ExternalState.Ready(count = sideEffect.toString()) }
                awaitSideEffect()
            }
        }.also {
            assertTrue { it.message?.startsWith("Expected External State but got SideEffectItem") == true }
        }
    }

    private inner class StateTestMiddleware(scope: TestScope) : ContainerHostWithExternalState<InternalState, ExternalState, Int> {
        override val container = scope.backgroundScope.container<InternalState, Int>(initialState).withExternalState(::transformState)

        private fun transformState(internalState: InternalState): ExternalState {
            return when (internalState) {
                InternalState.Loading -> ExternalState.Loading
                is InternalState.Ready -> ExternalState.Ready(count = internalState.count.toString())
            }
        }

        fun newCount(action: Int) = intent {
            reduce {
                InternalState.Ready(count = action)
            }
        }

        fun newSideEffect(action: Int) = intent {
            postSideEffect(action)
        }
    }

    private sealed interface InternalState {
        data object Loading : InternalState

        data class Ready(val count: Int) : InternalState
    }

    private sealed interface ExternalState {
        data object Loading : ExternalState

        data class Ready(val count: String) : ExternalState
    }
}
