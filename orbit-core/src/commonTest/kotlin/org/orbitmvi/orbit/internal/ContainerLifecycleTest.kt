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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ContainerLifecycleTest {

    @Test
    fun on_create_is_called_once_after_connecting_to_the_container() = runTest {
        val initialState = TestState()
        val middleware = Middleware(this, initialState)
        middleware.container.stateFlow.test {
            assertEquals(initialState, awaitItem())
        }
        middleware.container.sideEffectFlow.test {
            assertEquals(initialState.id.toString(), awaitItem())
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware(scope: TestScope, initialState: TestState) : ContainerHost<TestState, String> {

        override val container = scope.backgroundScope.container(initialState) {
            postSideEffect(state.id.toString())
        }
    }
}
