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
import org.orbitmvi.orbit.mapToExternalState
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class StateWithExternalStateTest {

    private val initialState = InternalState()

    @Test
    fun internal_succeeds_if_initial_state_matches_expected_state_with_expectState() = runTest {
        StateTestMiddleware(this).test(this, settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.INTERNAL_ONLY)) {
            expectInternalState { initialState }
        }
    }

    @Test
    fun external_succeeds_if_initial_state_matches_expected_state_with_expectState() = runTest {
        StateTestMiddleware(this).test(this, settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.EXTERNAL_ONLY)) {
            expectExternalState { containerHost.container.mapToExternalState(initialState) }
        }
    }

    @Test
    fun internal_and_external_succeeds_if_initial_state_matches_expected_state_with_expectState() = runTest {
        StateTestMiddleware(this).test(
            this,
            settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.INTERNAL_AND_EXTERNAL)
        ) {
            expectInternalState { initialState }
            expectExternalState { containerHost.container.mapToExternalState(initialState) }
        }
    }

    @Test
    fun internal_fails_if_initial_state_does_not_match_expected_state() = runTest {
        val someRandomState = InternalState()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.INTERNAL_ONLY)) {
                assertEquals(someRandomState, awaitInternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun external_fails_if_initial_state_does_not_match_expected_state() = runTest {
        val someRandomState = InternalState()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.EXTERNAL_ONLY)) {
                assertEquals(containerHost.container.mapToExternalState(someRandomState), awaitExternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_and_external_fails_if_internal_initial_state_does_not_match_expected_state() = runTest {
        val someRandomState = InternalState()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(
                this,
                settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.INTERNAL_AND_EXTERNAL)
            ) {
                assertEquals(someRandomState, awaitInternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_and_external_fails_if_external_initial_state_does_not_match_expected_state() = runTest {
        val someRandomState = InternalState()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(
                this,
                settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.INTERNAL_AND_EXTERNAL)
            ) {
                assertEquals(initialState, awaitInternalState())
                assertEquals(containerHost.container.mapToExternalState(someRandomState), awaitExternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_succeeds_if_emitted_states_match_expected_states() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
            containerHost.newCount(action)
            containerHost.newCount(action2)
            assertEquals(InternalState(count = action), awaitInternalState())
            assertEquals(InternalState(count = action2), awaitInternalState())
        }
    }

    @Test
    fun external_succeeds_if_emitted_states_match_expected_states() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
            containerHost.newCount(action)
            containerHost.newCount(action2)
            assertEquals(ExternalState(count = action.toString()), awaitExternalState())
            assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
        }
    }

    @Test
    fun internal_and_external_succeeds_if_emitted_states_match_expected_states() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
            containerHost.newCount(action)
            containerHost.newCount(action2)
            assertEquals(InternalState(count = action), awaitInternalState())
            assertEquals(ExternalState(count = action.toString()), awaitExternalState())
            assertEquals(InternalState(count = action2), awaitInternalState())
            assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
        }
    }

    @Test
    fun internal_fails_if_more_states_emitted_than_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                assertEquals(InternalState(count = action), awaitInternalState())
                assertEquals(InternalState(count = action2), awaitInternalState())
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
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                assertEquals(ExternalState(count = action.toString()), awaitExternalState())
                assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith("Unconsumed events found") == true }
        }
    }

    @Test
    fun internal_and_external_internal_fails_if_more_states_emitted_than_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                assertEquals(InternalState(count = action), awaitInternalState())
                assertEquals(ExternalState(count = action.toString()), awaitExternalState())
                assertEquals(InternalState(count = action2), awaitInternalState())
                assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith("Unconsumed events found") == true }
        }
    }

    @Test
    fun internal_and_external_external_fails_if_more_states_emitted_than_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                containerHost.newCount(action3)
                assertEquals(InternalState(count = action), awaitInternalState())
                assertEquals(ExternalState(count = action.toString()), awaitExternalState())
                assertEquals(InternalState(count = action2), awaitInternalState())
                assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
                assertEquals(InternalState(count = action3), awaitInternalState())
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
            StateTestMiddleware(this).test(this, timeout = 500.milliseconds, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(InternalState(count = action), awaitInternalState())
                assertEquals(InternalState(count = action2), awaitInternalState())
                assertEquals(InternalState(count = action3), awaitInternalState())
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
            StateTestMiddleware(this).test(this, timeout = 500.milliseconds, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(ExternalState(count = action.toString()), awaitExternalState())
                assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
                assertEquals(ExternalState(count = action3.toString()), awaitExternalState())
            }
        }.also {
            assertTrue(it.message?.startsWith("No value produced in ") == true)
        }
    }

    @Test
    fun internal_and_external_fails_if_one_more_internal_state_expected_than_emitted() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(
                this,
                timeout = 500.milliseconds,
                settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)
            ) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(InternalState(count = action), awaitInternalState())
                assertEquals(ExternalState(count = action.toString()), awaitExternalState())
                assertEquals(InternalState(count = action2), awaitInternalState())
                assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
                assertEquals(InternalState(count = action3), awaitInternalState())
            }
        }.also {
            assertTrue(it.message?.startsWith("No value produced in ") == true)
        }
    }

    @Test
    fun internal_and_external_fails_if_one_more_external_state_expected_than_emitted() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(
                this,
                timeout = 500.milliseconds,
                settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)
            ) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(InternalState(count = action), awaitInternalState())
                assertEquals(ExternalState(count = action.toString()), awaitExternalState())
                assertEquals(InternalState(count = action2), awaitInternalState())
                assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
                assertEquals(ExternalState(count = action3.toString()), awaitExternalState())
            }
        }.also {
            assertTrue(it.message?.startsWith("No value produced in ") == true)
        }
    }

    @Test
    fun internal_fails_if_first_emitted_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(InternalState(count = action2), awaitInternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun external_fails_if_first_emitted_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_and_external_fails_if_first_emitted_internal_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(InternalState(count = action2), awaitInternalState())
                //assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_and_external_fails_if_first_emitted_external_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                awaitInternalState()
                assertEquals(ExternalState(count = action2.toString()), awaitExternalState())
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
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(InternalState(count = action), awaitInternalState())
                assertEquals(InternalState(count = action3), awaitInternalState())
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
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(ExternalState(count = action.toString()), awaitExternalState())
                assertEquals(ExternalState(count = action3.toString()), awaitExternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_and_external_fails_if_second_emitted_internal_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(InternalState(count = action), awaitInternalState())
                assertEquals(ExternalState(count = action.toString()), awaitExternalState())
                assertEquals(InternalState(count = action3), awaitInternalState())
//                assertEquals(ExternalState(count = action3.toString()), awaitExternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_and_external_fails_if_second_emitted_external_state_after_initial_does_not_match_expected() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
                containerHost.newCount(action)
                containerHost.newCount(action2)
                assertEquals(InternalState(count = action), awaitInternalState())
                assertEquals(ExternalState(count = action.toString()), awaitExternalState())
                assertEquals(InternalState(count = action2), awaitInternalState())
                assertEquals(ExternalState(count = action3.toString()), awaitExternalState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun internal_fails_if_expected_a_state_but_got_a_side_effect() = runTest {
        val sideEffect = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
                containerHost.newSideEffect(sideEffect)
                containerHost.newCount(sideEffect)

                awaitInternalState()
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
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
                containerHost.newSideEffect(sideEffect)
                containerHost.newCount(sideEffect)

                awaitExternalState()
                awaitSideEffect()
            }
        }.also {
            assertTrue { it.message?.startsWith("Expected External State but got SideEffectItem") == true }
        }
    }

    @Test
    fun internal_and_external_fails_if_expected_an_internal_state_but_got_a_side_effect() = runTest {
        val sideEffect = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
                containerHost.newSideEffect(sideEffect)
                containerHost.newCount(sideEffect)

                awaitInternalState()
                awaitSideEffect()
            }
        }.also {
            assertTrue { it.message?.startsWith("Expected Internal State but got SideEffectItem") == true }
        }
    }

    @Test
    fun internal_and_external_fails_if_expected_an_external_state_but_got_a_side_effect() = runTest {
        val sideEffect = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
                containerHost.newSideEffect(sideEffect)
                containerHost.newCount(sideEffect)

                awaitExternalState()
                awaitSideEffect()
            }
        }.also {
            assertTrue { it.message?.startsWith("Expected External State but got SideEffectItem") == true }
        }
    }

    @Test
    fun internal_can_assert_state_changes_only() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
            containerHost.newList(action)
            containerHost.newList(action2)
            containerHost.newList(action3)
            expectInternalState { copy(list = listOf(action)) }
            expectInternalState { copy(list = listOf(action, action2)) }
            expectInternalState { copy(list = listOf(action, action2, action3)) }
        }
    }

    @Test
    fun external_can_assert_state_changes_only() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
            containerHost.newList(action)
            containerHost.newList(action2)
            containerHost.newList(action3)
            expectExternalState { copy(list = listOf(action.toString())) }
            expectExternalState { copy(list = listOf(action.toString(), action2.toString())) }
            expectExternalState { copy(list = listOf(action.toString(), action2.toString(), action3.toString())) }
        }
    }

    @Test
    fun internal_can_assert_state_changes_only__shorthand() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
            containerHost.newList(action)
            containerHost.newList(action2)
            containerHost.newList(action3)
            expectInternalState(InternalState(count = initialState.count, list = listOf(action)))
            expectInternalState(InternalState(count = initialState.count, list = listOf(action, action2)))
            expectInternalState(InternalState(count = initialState.count, list = listOf(action, action2, action3)))
        }
    }

    @Test
    fun external_can_assert_state_changes_only__shorthand() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
            containerHost.newList(action)
            containerHost.newList(action2)
            containerHost.newList(action3)
            expectExternalState(ExternalState(count = initialState.count.toString(), list = listOf(action.toString())))
            expectExternalState(ExternalState(count = initialState.count.toString(), list = listOf(action.toString(), action2.toString())))
            expectExternalState(
                ExternalState(
                    count = initialState.count.toString(),
                    list = listOf(action.toString(), action2.toString(), action3.toString())
                )
            )
        }
    }

    @Test
    fun internal_can_assert_state_changes_only__failure() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
                containerHost.newList(action)
                containerHost.newList(action2)
                containerHost.newList(action3)
                expectInternalState { copy(list = listOf(action)) }
                expectInternalState { copy(list = listOf(action, action2, action3)) }
            }
        }
    }

    @Test
    fun external_can_assert_state_changes_only__failure() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
                containerHost.newList(action)
                containerHost.newList(action2)
                containerHost.newList(action3)
                expectExternalState { copy(list = listOf(action.toString())) }
                expectExternalState { copy(list = listOf(action, action2, action3).map(Int::toString)) }
            }
        }
    }

    @Test
    fun internal_can_assert_state_changes_only__shorthand_failure() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
                containerHost.newList(action)
                containerHost.newList(action2)
                containerHost.newList(action3)
                expectInternalState(InternalState(count = initialState.count, list = listOf(action)))
                expectInternalState(InternalState(count = initialState.count, list = listOf(action, action2, action3)))
                //expectInternalState(InternalState(count = initialState.count, list = listOf(action, action2)))
            }
        }
    }

    @Test
    fun external_can_assert_state_changes_only__shorthand_failure() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
                containerHost.newList(action)
                containerHost.newList(action2)
                containerHost.newList(action3)
                expectExternalState(ExternalState(count = initialState.count.toString(), list = listOf(action.toString())))
                expectExternalState(
                    ExternalState(
                        count = initialState.count.toString(),
                        list = listOf(action, action2, action3).map(Int::toString)
                    )
                )
            }
        }
    }

    private inner class StateTestMiddleware(scope: TestScope) :
        ContainerHostWithExternalState<InternalState, ExternalState, Int> {
        override val container = scope.backgroundScope.container<InternalState, Int>(initialState).mapToExternalState(::mapToExternalState)
        private fun mapToExternalState(internalState: InternalState): ExternalState =
            ExternalState(internalState.count.toString(), internalState.list.map { it.toString() })

        fun newCount(action: Int) = intent {
            reduce {
                InternalState(count = action)
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

    private data class InternalState(
        val count: Int = Random.nextInt(),
        val list: List<Int> = emptyList()
    )

    private data class ExternalState(
        val count: String,
        val list: List<String> = emptyList()
    )
}
