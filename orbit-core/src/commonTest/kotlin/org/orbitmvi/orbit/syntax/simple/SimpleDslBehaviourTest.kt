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

import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.assert
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class SimpleDslBehaviourTest {

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    private val initialState = TestState()

    @Test
    fun `reducer produces new states`() {
        val action = Random.nextInt()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.reducer(action)

        middleware.assert(initialState) {
            states(
                { TestState(action) }
            )
        }
    }

    @Test
    fun `transformer maps values`() {
        val action = Random.nextInt()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.transformer(action)

        middleware.assert(initialState) {
            states(
                { TestState(action + 5) }
            )
        }
    }

    @Test
    fun `posting side effects emit side effects`() {
        val action = Random.nextInt()
        val middleware = BaseDslMiddleware().test(initialState)

        middleware.postingSideEffect(action)

        middleware.assert(initialState) {
            postedSideEffects(action.toString())
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class BaseDslMiddleware : ContainerHost<TestState, String> {
        override var container = scope.container<TestState, String>(TestState(42))

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
