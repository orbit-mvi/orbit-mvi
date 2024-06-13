/*
 * Copyright 2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.internal

import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHostWithExtState
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ExtStateTest {

    @Test
    fun initial_state_is_emitted_on_connection() = runTest {
        val initialState = TestInternalState()
        val middleware = Middleware(this, initialState)
        middleware.container.extStateFlow.test {
            assertEquals(initialState.toExternal(), awaitItem())
        }
    }

    @Test
    fun latest_state_is_emitted_on_connection() = runTest {
        val initialState = TestInternalState()
        val middleware = Middleware(this, initialState)
        val action = Random.nextInt()
        middleware.container.extStateFlow.test {
            middleware.something(action)
            assertEquals(initialState.toExternal(), awaitItem())
            assertEquals(TestInternalState(action).toExternal(), awaitItem())
        }

        middleware.container.extStateFlow.test {
            assertEquals(TestInternalState(action).toExternal(), awaitItem())
        }
    }

    @Test
    fun current_state_is_set_to_the_initial_state_after_instantiation() = runTest {
        val initialState = TestInternalState()
        val middleware = Middleware(this, initialState)

        assertEquals(initialState.toExternal(), middleware.container.extStateFlow.value)
    }

    @Test
    fun current_state_is_up_to_date_after_modification() = runTest {
        val initialState = TestInternalState()
        val middleware = Middleware(this, initialState)
        val action = Random.nextInt()
        middleware.container.extStateFlow.test {
            skipItems(1)
            middleware.something(action).join()

            val newState = awaitItem()

            assertEquals(middleware.container.extStateFlow.value, newState)
        }
    }

    private data class TestInternalState(val id: Int = Random.nextInt())
    private data class TestExternalState(val id: Int)

    private fun TestInternalState.toExternal() = TestExternalState(id)

    private inner class Middleware(scope: TestScope, initialState: TestInternalState) :
        ContainerHostWithExtState<TestInternalState, String, TestExternalState> {
        override val container = scope.backgroundScope.container<TestInternalState, String>(initialState)
            .withExtState { it.toExternal() }

        fun something(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }
    }
}
