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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.test.assertContains
import kotlin.random.Random
import kotlin.test.assertFailsWith

@ExperimentalCoroutinesApi
internal class ParameterisedStateTest(blocking: Boolean) {
    companion object {
        const val TIMEOUT = 1000L
    }

    private val initialState = State()

    private val scope = CoroutineScope(Job())
    private val testSubject = if (blocking) {
        StateTestMiddleware().test(
            initialState = initialState
        )
    } else {
        StateTestMiddleware().liveTest(initialState)
    }

    fun cancel() {
        scope.cancel()
    }

    fun `succeeds if initial state matches expected state`() {
        testSubject.stateObserver.awaitCount(1)

        testSubject.assert(initialState)
    }

    fun `fails if initial state does not match expected state`() {
        val someRandomState = State()

        testSubject.stateObserver.awaitCount(1)

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(someRandomState)
        }

        throwable.message.assertContains(
            "<${Regex.escape(someRandomState.toString())}>[^<]*<${Regex.escape(initialState.toString())}>".toRegex()
        )
    }

    fun `succeeds if emitted states match expected states`() {
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

    fun `fails if more states emitted than expected`() {
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

    fun `fails if one more state expected than emitted`() {
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

    fun `fails if two more states expected than emitted`() {
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

    fun `fails if first emitted state does not match expected`() {
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

    fun `fails if second emitted state does not match expected`() {
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

    fun `fails if expected states are out of order`() {
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

    fun `succeeds with dropped assertions`() {
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

    fun `fails if dropped assertions mean extra states are observed`() {
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

    private inner class StateTestMiddleware :
        ContainerHost<State, Nothing> {
        override val container = scope.container<State, Nothing>(initialState)

        fun something(action: Int): Unit = intent {
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
