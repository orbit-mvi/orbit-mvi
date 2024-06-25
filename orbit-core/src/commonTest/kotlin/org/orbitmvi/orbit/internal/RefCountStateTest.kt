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
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RefCountStateTest {

    @Test
    fun initial_state_is_emitted_on_connection() = runTest {
        val initialState = TestState()
        val middleware = Middleware(this, initialState)
        middleware.container.refCountStateFlow.test {
            assertEquals(initialState, awaitItem())
        }
    }

    @Test
    fun latest_state_is_emitted_on_connection() = runTest {
        val initialState = TestState()
        val middleware = Middleware(this, initialState)
        val action = Random.nextInt()
        middleware.container.refCountStateFlow.test {
            middleware.something(action)
            assertEquals(initialState, awaitItem())
            assertEquals(TestState(action), awaitItem())
        }

        middleware.container.refCountStateFlow.test {
            assertEquals(TestState(action), awaitItem())
        }
    }

    @Test
    fun current_state_is_set_to_the_initial_state_after_instantiation() = runTest {
        val initialState = TestState()
        val middleware = Middleware(this, initialState)

        assertEquals(initialState, middleware.container.refCountStateFlow.value)
    }

    @Test
    fun current_state_is_up_to_date_after_modification() = runTest {
        val initialState = TestState()
        val middleware = Middleware(this, initialState)
        val action = Random.nextInt()
        middleware.container.refCountStateFlow.test {
            skipItems(1)
            middleware.something(action).join()

            assertEquals(middleware.container.refCountStateFlow.value, awaitItem())
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware(scope: TestScope, initialState: TestState) : ContainerHost<TestState, String> {
        override val container = scope.backgroundScope.container<TestState, String>(initialState)

        fun something(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }
    }
}
