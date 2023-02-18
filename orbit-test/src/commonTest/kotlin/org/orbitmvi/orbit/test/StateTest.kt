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

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce

@OptIn(OrbitExperimental::class)
@ExperimentalCoroutinesApi
class StateTest {

    private val initialState = State()

    @Test
    fun `succeeds if initial state matches expected state`() = runTest {
        StateTestMiddleware(this).test(this) {
            expectInitialState()
        }
    }

    @Test
    fun `fails if initial state does not match expected state`() = runTest {
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
    fun `succeeds if emitted states match expected states`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()

        StateTestMiddleware(this).test(this) {
            expectInitialState()
            invokeIntent { newCount(action) }
            invokeIntent { newCount(action2) }
            assertEquals(State(count = action), awaitState())
            assertEquals(State(count = action2), awaitState())
        }
    }

    @Test
    fun `fails if more states emitted than expected`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                invokeIntent { newCount(action) }
                invokeIntent { newCount(action2) }
                invokeIntent { newCount(action3) }
                assertEquals(State(count = action), awaitState())
                assertEquals(State(count = action2), awaitState())
            }
        }.also {
            assertTrue { it.message?.startsWith("Unconsumed events found") == true }
        }
    }

    @Test
    fun `fails if one more state expected than emitted`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                invokeIntent { newCount(action) }
                invokeIntent { newCount(action2) }
                assertEquals(State(count = action), awaitState())
                assertEquals(State(count = action2), awaitState())
                assertEquals(State(count = action3), awaitState())
            }
        }.also {
            assertEquals("No value produced in 1s", it.message)
        }
    }

    @Test
    fun `fails if first emitted state after initial does not match expected`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                invokeIntent { newCount(action) }
                invokeIntent { newCount(action2) }
                assertEquals(State(count = action2), awaitState())
                assertEquals(State(count = action3), awaitState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun `fails if second emitted state after initial does not match expected`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                invokeIntent { newCount(action) }
                invokeIntent { newCount(action2) }
                assertEquals(State(count = action), awaitState())
                assertEquals(State(count = action3), awaitState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun `fails if expected states are out of order`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                invokeIntent { newCount(action) }
                invokeIntent { newCount(action2) }
                invokeIntent { newCount(action3) }
                assertEquals(State(count = action), awaitState())
                assertEquals(State(count = action3), awaitState())
                assertEquals(State(count = action2), awaitState())
            }
        }.also {
            assertTrue { it.message?.startsWith(prefix = "expected", ignoreCase = true) == true }
        }
    }

    @Test
    fun `fails if expected a state but got a side effect`() = runTest {
        val sideEffect = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                invokeIntent { newSideEffect(sideEffect) }
                invokeIntent { newCount(sideEffect) }

                expectInitialState()
                awaitState()
                awaitSideEffect()
            }
        }.also {
            assertTrue { it.message?.startsWith("Expected State but got SideEffectItem") == true }
        }
    }

    @Test
    fun `can assert state changes only`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        StateTestMiddleware(this).test(this) {
            expectInitialState()
            invokeIntent { newList(action) }
            invokeIntent { newList(action2) }
            invokeIntent { newList(action3) }
            expectState { copy(list = listOf(action)) }
            expectState { copy(list = listOf(action, action2)) }
            expectState { copy(list = listOf(action, action2, action3)) }
        }
    }

    @Test
    fun `can assert state changes only - failure`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        assertFailsWith<AssertionError> {
            StateTestMiddleware(this).test(this) {
                expectInitialState()
                invokeIntent { newList(action) }
                invokeIntent { newList(action2) }
                invokeIntent { newList(action3) }
                expectState { copy(list = listOf(action)) }
                expectState { copy(list = listOf(action, action2, action3)) }
                expectState { copy(list = listOf(action, action2)) }
            }
        }
    }

    private inner class StateTestMiddleware(scope: CoroutineScope) :
        ContainerHost<State, Int> {
        override val container = scope.container<State, Int>(initialState)

        fun newCount(action: Int): Unit = intent {
            reduce {
                State(count = action)
            }
        }

        fun newList(action: Int): Unit = intent {
            reduce {
                state.copy(list = state.list + action)
            }
        }

        fun newSideEffect(action: Int): Unit = intent {
            postSideEffect(action)
        }
    }

    private data class State(
        val count: Int = Random.nextInt(),
        val list: List<Int> = emptyList()
    )
}
