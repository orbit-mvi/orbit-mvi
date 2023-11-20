/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.test.assertContains
import kotlin.random.Random
import kotlin.test.assertFailsWith

@Suppress("DEPRECATION")
@ExperimentalCoroutinesApi
internal class ParameterisedStateTest(private val blocking: Boolean) {
    companion object {
        const val TIMEOUT = 1000L
    }

    private val initialState = State()

    private fun testSubject(scope: TestScope) = if (blocking) {
        StateTestMiddleware(scope).test(
            initialState = initialState
        )
    } else {
        StateTestMiddleware(scope).liveTest(initialState)
    }

    fun succeeds_if_initial_state_matches_expected_state() = runTest {
        val testSubject = testSubject(this)
        testSubject.stateObserver.awaitCount(1)

        testSubject.assert(initialState)
    }

    fun fails_if_initial_state_does_not_match_expected_state() = runTest {
        val testSubject = testSubject(this)
        val someRandomState = State()

        testSubject.stateObserver.awaitCount(1)

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(someRandomState)
        }

        throwable.message.assertContains(
            "<${Regex.escape(someRandomState.toString())}>[^<]*<${Regex.escape(initialState.toString())}>".toRegex()
        )
    }

    fun succeeds_if_emitted_states_match_expected_states() = runTest {
        val testSubject = testSubject(this)
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        testSubject.call { something(action) }
        testSubject.stateObserver.awaitCount(2)
        testSubject.call { something(action2) }
        testSubject.stateObserver.awaitCount(3)

        testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
            states(
                { State(count = action) },
                { State(count = action2) }
            )
        }
    }

    fun fails_if_more_states_emitted_than_expected() = runTest {
        val testSubject = testSubject(this)
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        testSubject.call { something(action) }
        testSubject.stateObserver.awaitCount(2)
        testSubject.call { something(action2) }
        testSubject.stateObserver.awaitCount(3)

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { State(count = action) }
                )
            }
        }

        throwable.message.assertContains(
            "Expected 1 states but more were emitted:\n" +
                "[State(count=$action2)]"
        )
    }

    fun fails_if_one_more_state_expected_than_emitted() = runTest {
        val testSubject = testSubject(this)
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        testSubject.call { something(action) }
        testSubject.stateObserver.awaitCount(2)
        testSubject.call { something(action2) }
        testSubject.stateObserver.awaitCount(3)

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = 1000L) {
                states(
                    { State(count = action) },
                    { State(count = action2) },
                    { State(count = action3) }
                )
            }
        }

        throwable.message.assertContains(
            "Failed assertions at indices 2..2, expected states but never received:\n" +
                "[State(count=$action3)]"
        )
    }

    fun fails_if_two_more_states_expected_than_emitted() = runTest {
        val testSubject = testSubject(this)
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val action4 = Random.nextInt()

        testSubject.call { something(action) }
        testSubject.stateObserver.awaitCount(2)
        testSubject.call { something(action2) }
        testSubject.stateObserver.awaitCount(3)

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = 1000L) {
                states(
                    { State(count = action) },
                    { State(count = action2) },
                    { State(count = action3) },
                    { State(count = action4) }
                )
            }
        }

        throwable.message.assertContains(
            "Failed assertions at indices 2..3, expected states but never received:\n" +
                "[State(count=$action3), State(count=$action4)]"
        )
    }

    fun fails_if_first_emitted_state_does_not_match_expected() = runTest {
        val testSubject = testSubject(this)
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        testSubject.call { something(action) }
        testSubject.stateObserver.awaitCount(2)
        testSubject.call { something(action2) }
        testSubject.stateObserver.awaitCount(3)

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { State(count = action2) },
                    { State(count = action3) }
                )
            }
        }

        throwable.message.assertContains(
            "Failed assertion at index 0[^<]*<State\\(count=$action2\\)>[^<]*<State\\(count=$action\\)>".toRegex()
        )
    }

    fun fails_if_second_emitted_state_does_not_match_expected() = runTest {
        val testSubject = testSubject(this)
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        testSubject.call { something(action) }
        testSubject.stateObserver.awaitCount(2)
        testSubject.call { something(action2) }
        testSubject.stateObserver.awaitCount(3)

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { State(count = action2) },
                    { State(count = action3) }
                )
            }
        }

        throwable.message.assertContains(
            "Failed assertion at index 0[^<]*<State\\(count=$action2\\)>[^<]*<State\\(count=$action\\)>".toRegex()
        )
    }

    fun fails_if_expected_states_are_out_of_order() = runTest {
        val testSubject = testSubject(this)
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        testSubject.call { something(action) }
        testSubject.stateObserver.awaitCount(2)
        testSubject.call { something(action2) }
        testSubject.stateObserver.awaitCount(3)

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { State(count = action2) },
                    { State(count = action) }
                )
            }
        }

        throwable.message.assertContains(
            "Failed assertion at index 0[^<]*<State\\(count=$action2\\)>[^<]*<State\\(count=$action\\)>".toRegex()
        )
    }

    fun succeeds_with_dropped_assertions() = runTest {
        val testSubject = testSubject(this)
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        testSubject.call { something(action) }
        testSubject.stateObserver.awaitCount(2)
        testSubject.call { something(action2) }
        testSubject.stateObserver.awaitCount(3)
        testSubject.call { something(action3) }
        testSubject.stateObserver.awaitCount(4)

        testSubject.assert(initialState, timeoutMillis = 2000L) {
            states(
                { State(count = action) },
                { State(count = action2) },
                { State(count = action2) },
                { State(count = action3) }
            )
        }
    }

    fun fails_if_dropped_assertions_mean_extra_states_are_observed() = runTest {
        val testSubject = testSubject(this)
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        testSubject.call { something(action) }
        testSubject.stateObserver.awaitCount(2)
        testSubject.call { something(action2) }
        testSubject.stateObserver.awaitCount(3)

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { initialState },
                    { State(count = action) }
                )
            }
        }

        throwable.message.assertContains(
            "Expected 2 states but more were emitted:\n" +
                "[State(count=$action2)]\n\n" +
                "Caution: 1 assertions were dropped as they encountered a current state " +
                "which already satisfied them."
        )
    }

    private inner class StateTestMiddleware(scope: TestScope) :
        ContainerHost<State, Nothing> {
        override val container = scope.backgroundScope.container<State, Nothing>(initialState)

        fun something(action: Int) = intent {
            reduce {
                State(count = action)
            }
        }
    }

    private data class State(val count: Int = Random.nextInt())

    private fun <STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>> TestContainerHost<STATE, SIDE_EFFECT, T>.call(
        block: T.() -> Unit
    ) {
        when (this) {
            is SuspendingTestContainerHost -> runBlocking { testIntent { block() } }
            is RegularTestContainerHost -> testIntent { block() }
        }
    }
}
