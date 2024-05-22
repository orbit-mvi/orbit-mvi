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

package org.orbitmvi.orbit.internal

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StateTest {

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun initial_state_is_emitted_on_connection() = runTest {
        val initialState = TestState()
        val middleware = Middleware(initialState)
        middleware.container.stateFlow.test {
            assertEquals(initialState, awaitItem())
        }
    }

    @Test
    fun latest_state_is_emitted_on_connection() = runTest {
        val initialState = TestState()
        val action = Random.nextInt()
        val middleware = Middleware(initialState)

        middleware.container.stateFlow.test {
            assertEquals(initialState, awaitItem())
            middleware.something(action)
            assertEquals(TestState(action), awaitItem())
        }

        middleware.container.stateFlow.test {
            assertEquals(TestState(action), awaitItem())
        }
    }

    @Test
    fun current_state_is_set_to_the_initial_state_after_instantiation() {
        val initialState = TestState()
        val middleware = Middleware(initialState)

        assertEquals(initialState, middleware.container.stateFlow.value)
    }

    @Test
    fun current_state_is_up_to_date_after_modification() = runTest {
        val initialState = TestState()
        val middleware = Middleware(initialState)
        val action = Random.nextInt()
        middleware.container.stateFlow.test {
            middleware.something(action)
            skipItems(2)
        }

        assertEquals(middleware.container.stateFlow.value, middleware.container.stateFlow.value)
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware(initialState: TestState) : ContainerHost<TestState, String> {
        override val container = scope.container<TestState, String>(initialState)

        fun something(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }
    }
}
