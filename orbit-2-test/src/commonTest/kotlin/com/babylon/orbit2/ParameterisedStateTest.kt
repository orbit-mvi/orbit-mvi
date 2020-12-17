/*
 * Stateright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a State of the License at
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

import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.syntax.strict.reduce
import com.babylon.orbit2.syntax.strict.transform
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlin.random.Random
import kotlin.test.AfterTest

@ExperimentalCoroutinesApi
internal class ParameterisedStateTest(blocking: Boolean) {
    companion object {
        const val TIMEOUT = 1000L
    }

    private val initialState = State()

    private val scope = CoroutineScope(Job())
    private val testSubject = StateTestMiddleware().test(
        initialState = initialState,
        isolateFlow = false,
        blocking = blocking
    )

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    fun `succeeds if initial state matches expected state`() {
        val testStateObserver = testSubject.container.stateFlow.test()
        testStateObserver.awaitCount(1)

        testSubject.assert(initialState)
    }

    fun `fails if initial state does not match expected state`() {
        val someRandomState = State()

        val testStateObserver = testSubject.container.stateFlow.test()
        testStateObserver.awaitCount(1)

        val throwable = shouldThrow<AssertionError> {
            testSubject.assert(someRandomState)
        }

        throwable.message.shouldContain(
            "expected:<$someRandomState> but was:<$initialState>"
        )
    }

    fun `succeeds if emitted states match expected states`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        val testStateObserver = testSubject.container.stateFlow.test()
        testSubject.something(action)
        testStateObserver.awaitCount(2)
        testSubject.something(action2)
        testStateObserver.awaitCount(3)

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

        val testStateObserver = testSubject.container.stateFlow.test()
        testSubject.something(action)
        testStateObserver.awaitCount(2)
        testSubject.something(action2)
        testStateObserver.awaitCount(3)

        val throwable = shouldThrow<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { State(count = action) }
                )
            }
        }

        throwable.message.shouldContain(
            "Expected 1 states but more were emitted:\n" +
                    "[State(count=$action2)]"
        )
    }

    fun `fails if one more state expected than emitted`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        val testStateObserver = testSubject.container.stateFlow.test()
        testSubject.something(action)
        testStateObserver.awaitCount(2)
        testSubject.something(action2)
        testStateObserver.awaitCount(3)

        val throwable = shouldThrow<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = 1000L) {
                states(
                    { State(count = action) },
                    { State(count = action2) },
                    { State(count = action3) }
                )
            }
        }

        throwable.message.shouldContain(
            "Failed assertions at indices 2..2, expected states but never received:\n" +
                    "[State(count=$action3)]"
        )
    }

    fun `fails if two more states expected than emitted`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val action4 = Random.nextInt()

        val testStateObserver = testSubject.container.stateFlow.test()
        testSubject.something(action)
        testStateObserver.awaitCount(2)
        testSubject.something(action2)
        testStateObserver.awaitCount(3)

        val throwable = shouldThrow<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = 1000L) {
                states(
                    { State(count = action) },
                    { State(count = action2) },
                    { State(count = action3) },
                    { State(count = action4) }
                )
            }
        }

        throwable.message.shouldContain(
            "Failed assertions at indices 2..3, expected states but never received:\n" +
                    "[State(count=$action3), State(count=$action4)]"
        )
    }

    fun `fails if first emitted state does not match expected`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        val testStateObserver = testSubject.container.stateFlow.test()
        testSubject.something(action)
        testStateObserver.awaitCount(2)
        testSubject.something(action2)
        testStateObserver.awaitCount(3)

        val throwable = shouldThrow<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { State(count = action2) },
                    { State(count = action3) }
                )
            }
        }

        throwable.message.shouldContain(
            "Failed assertion at index 0 " +
                    "expected:<State(count=$action2)> but was:<State(count=$action)>"
        )
    }

    fun `fails if second emitted state does not match expected`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        val testStateObserver = testSubject.container.stateFlow.test()
        testSubject.something(action)
        testStateObserver.awaitCount(2)
        testSubject.something(action2)
        testStateObserver.awaitCount(3)

        val throwable = shouldThrow<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { State(count = action2) },
                    { State(count = action3) }
                )
            }
        }

        throwable.shouldHaveMessage(
            "Failed assertion at index 0 expected:<State(count=$action2)> but was:<State(count=$action)>"
        )
    }

    fun `fails if expected states are out of order`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        val testStateObserver = testSubject.container.stateFlow.test()
        testSubject.something(action)
        testStateObserver.awaitCount(2)
        testSubject.something(action2)
        testStateObserver.awaitCount(3)

        val throwable = shouldThrow<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { State(count = action2) },
                    { State(count = action) }
                )
            }
        }

        throwable.message.shouldContain(
            "Failed assertion at index 0 " +
                    "expected:<State(count=$action2)> but was:<State(count=$action)>"
        )
    }

    fun `succeeds with dropped assertions`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        val testStateObserver = testSubject.container.stateFlow.test()
        testSubject.something(action)
        testStateObserver.awaitCount(2)
        testSubject.something(action2)
        testStateObserver.awaitCount(3)
        testSubject.something(action3)
        testStateObserver.awaitCount(4)

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

        val testStateObserver = testSubject.container.stateFlow.test()
        testSubject.something(action)
        testStateObserver.awaitCount(2)
        testSubject.something(action2)
        testStateObserver.awaitCount(3)

        val throwable = shouldThrow<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { initialState },
                    { State(count = action) }
                )
            }
        }

        throwable.message.shouldContain(
            "Expected 2 states but more were emitted:\n" +
                    "[State(count=$action2)]\n\n" +
                    "Caution: 1 assertions were dropped as they encountered a current state " +
                    "which already satisfied them."
        )
    }

    private inner class StateTestMiddleware :
        ContainerHost<State, Nothing> {
        override var container = scope.container<State, Nothing>(initialState)

        fun something(action: Int): Unit = orbit {
            transform {
                action.toString()
            }
                .reduce {
                    State(count = event.toInt())
                }
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
