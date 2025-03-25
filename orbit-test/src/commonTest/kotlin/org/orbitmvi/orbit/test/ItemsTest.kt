/*
 * Copyright 2023-2025 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ItemsTest {

    private val initialState = State()

    @Test
    fun items_can_be_skipped() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).test(this) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            skipItems(3)
            assertEquals(4, awaitSideEffect())
        }
    }

    @Test
    fun items_can_be_retrieved() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).test(this) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            assertEquals(Item.StateItem(State(1)), awaitItem())
            assertEquals(Item.SideEffectItem(3), awaitItem())
            assertEquals(Item.StateItem(State(2)), awaitItem())
            assertEquals(Item.SideEffectItem(4), awaitItem())
        }
    }

    @Test
    fun correctly_expects_no_items() = runTest {
        ItemTestMiddleware(this).test(this) {
            expectNoItems()
        }
    }

    @Test
    fun expects_no_items_fails_when_there_are_unconsumed_items() = runTest {
        ItemTestMiddleware(this).test(this, settings = TestSettings(autoCheckInitialState = false)) {
            assertFails { expectNoItems() }
        }
    }

    private inner class ItemTestMiddleware(scope: TestScope) :
        ContainerHost<State, Int> {
        override val container = scope.backgroundScope.container<State, Int>(initialState)

        fun newState(action: Int) = intent {
            reduce {
                State(count = action)
            }
        }

        fun newSideEffect(action: Int) = intent {
            postSideEffect(action)
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
