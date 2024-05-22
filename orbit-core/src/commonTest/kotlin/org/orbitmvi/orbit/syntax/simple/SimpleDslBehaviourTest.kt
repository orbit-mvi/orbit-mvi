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

package org.orbitmvi.orbit.syntax.simple

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test.test
import kotlin.random.Random
import kotlin.test.Test

internal class SimpleDslBehaviourTest {

    private val initialState = TestState()

    @Test
    fun `reducer produces new states`() = runTest {
        val action = Random.nextInt()
        BaseDslMiddleware(this).test(this, initialState) {
            expectInitialState()

            containerHost.reducer(action)

            expectState { TestState(action) }
        }
    }

    @Test
    fun `transformer maps values`() = runTest {
        val action = Random.nextInt()
        BaseDslMiddleware(this).test(this, initialState) {
            expectInitialState()

            containerHost.transformer(action)

            expectState { TestState(action + 5) }
        }
    }

    @Test
    fun `posting side effects emit side effects`() = runTest {
        val action = Random.nextInt()
        BaseDslMiddleware(this).test(this, initialState) {
            expectInitialState()

            containerHost.postingSideEffect(action)

            expectSideEffect(action.toString())
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class BaseDslMiddleware(scope: TestScope) : ContainerHost<TestState, String> {
        override val container = scope.backgroundScope.container<TestState, String>(TestState(42))

        fun reducer(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }

        fun transformer(action: Int) = intent {
            val newAction = action + dataSource()

            reduce {
                state.copy(id = newAction)
            }
        }

        fun postingSideEffect(action: Int) = intent {
            postSideEffect(action.toString())
        }

        private suspend fun dataSource(): Int {
            delay(100)
            return 5
        }
    }
}
