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

package org.orbitmvi.orbit.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(OrbitExperimental::class)
@ExperimentalCoroutinesApi
class ItemsTest {

    private val initialState = State()

    @Test
    fun `items can be skipped`() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).test(this) {
            expectInitialState()
            invokeIntent { newState(state1) }
            invokeIntent { newSideEffect(sideEffect1) }
            invokeIntent { newState(state2) }
            invokeIntent { newSideEffect(sideEffect2) }

            skipItems(3)
            assertEquals(4, awaitSideEffect())
        }
    }

    @Test
    fun `items can be retrieved`() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).test(this) {
            expectInitialState()
            invokeIntent { newState(state1) }
            invokeIntent { newSideEffect(sideEffect1) }
            invokeIntent { newState(state2) }
            invokeIntent { newSideEffect(sideEffect2) }

            assertEquals(Item.StateItem(State(1)), awaitItem())
            assertEquals(Item.SideEffectItem(3), awaitItem())
            assertEquals(Item.StateItem(State(2)), awaitItem())
            assertEquals(Item.SideEffectItem(4), awaitItem())
        }
    }

    private inner class ItemTestMiddleware(scope: CoroutineScope) :
        ContainerHost<State, Int> {
        override val container = scope.container<State, Int>(initialState)

        fun newState(action: Int): Unit = intent {
            reduce {
                State(count = action)
            }
        }

        fun newSideEffect(action: Int): Unit = intent {
            postSideEffect(action)
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
