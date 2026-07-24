/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.orbitContainer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.fail

class ItemsWithExternalStateTest {

    private val initialState = InternalState()

    @Test
    fun internal_items_can_be_skipped() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).testWithInternalState(this) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            skipItems(3)
            assertEquals(4, awaitSideEffect())
        }
    }

    @Test
    fun external_items_can_be_skipped() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).testWithExternalState(this) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            skipItems(3)
            assertEquals(4, awaitSideEffect())
        }
    }

    @Test
    fun internal_and_external_items_can_be_skipped() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).testWithInternalAndExternalState(this) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            skipItems(5)
            assertEquals(4, awaitSideEffect())
        }
    }

    @Test
    fun internal_items_can_be_retrieved() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).testWithInternalState(this) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            assertEquals(Item.StateItem(InternalState(1)), awaitItem())
            assertEquals(Item.SideEffectItem(3), awaitItem())
            assertEquals(Item.StateItem(InternalState(2)), awaitItem())
            assertEquals(Item.SideEffectItem(4), awaitItem())
        }
    }

    @Test
    fun external_items_can_be_retrieved() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).testWithExternalState(this) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            assertEquals(Item.StateItem(ExternalState("1")), awaitItem())
            assertEquals(Item.SideEffectItem(3), awaitItem())
            assertEquals(Item.StateItem(ExternalState("2")), awaitItem())
            assertEquals(Item.SideEffectItem(4), awaitItem())
        }
    }

    @Test
    fun internal_and_external_items_can_be_retrieved() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).testWithInternalAndExternalState(this) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            assertEquals(ItemWithInternalAndExternalState.InternalStateItem(InternalState(1)), awaitRawItem())
            assertEquals(ItemWithInternalAndExternalState.ExternalStateItem(ExternalState("1")), awaitRawItem())
            assertEquals(ItemWithInternalAndExternalState.SideEffectItem(3), awaitRawItem())
            assertEquals(ItemWithInternalAndExternalState.InternalStateItem(InternalState(2)), awaitRawItem())
            assertEquals(ItemWithInternalAndExternalState.ExternalStateItem(ExternalState("2")), awaitRawItem())
            assertEquals(ItemWithInternalAndExternalState.SideEffectItem(4), awaitRawItem())
        }
    }

    @Test
    fun internal_items_can_be_drained_until_a_side_effect() = runTest {
        ItemTestMiddleware(this).testWithInternalState(this) {
            containerHost.newState(1)
            containerHost.newState(2)
            containerHost.newSideEffect(3)

            var latest: InternalState? = null
            repeat(10) {
                when (val item = awaitItem()) {
                    is Item.StateItem -> latest = item.value
                    is Item.SideEffectItem -> {
                        assertEquals(3, item.value)
                        assertEquals(InternalState(2), latest)
                        return@testWithInternalState
                    }
                }
            }
            fail("Expected a side effect but none was received")
        }
    }

    @Test
    fun external_items_can_be_drained_until_a_side_effect() = runTest {
        ItemTestMiddleware(this).testWithExternalState(this) {
            containerHost.newState(1)
            containerHost.newState(2)
            containerHost.newSideEffect(3)

            var latest: ExternalState? = null
            repeat(10) {
                when (val item = awaitItem()) {
                    is Item.StateItem -> latest = item.value
                    is Item.SideEffectItem -> {
                        assertEquals(3, item.value)
                        assertEquals(ExternalState("2"), latest)
                        return@testWithExternalState
                    }
                }
            }
            fail("Expected a side effect but none was received")
        }
    }

    @Test
    fun await_item_advances_internal_state_for_relative_assertions() = runTest {
        ItemTestMiddleware(this).testWithInternalState(this) {
            containerHost.newState(1)
            containerHost.newState(2)

            // Consuming a state via awaitItem must advance the baseline used by relative assertions
            assertEquals(Item.StateItem(InternalState(1)), awaitItem())
            expectInternalState { copy(count = count + 1) }
        }
    }

    @Test
    fun await_item_advances_external_state_for_relative_assertions() = runTest {
        ItemTestMiddleware(this).testWithExternalState(this) {
            containerHost.newState(1)
            containerHost.newState(2)

            // Consuming a state via awaitItem must advance the baseline used by relative assertions
            assertEquals(Item.StateItem(ExternalState("1")), awaitItem())
            expectExternalState { copy(count = (count.toInt() + 1).toString()) }
        }
    }

    @Test
    fun internal_correctly_expects_no_items() = runTest {
        ItemTestMiddleware(this).testWithInternalState(this) {
            expectNoItems()
        }
    }

    @Test
    fun external_correctly_expects_no_items() = runTest {
        ItemTestMiddleware(this).testWithExternalState(this) {
            expectNoItems()
        }
    }

    @Test
    fun internal_and_external_correctly_expects_no_items() = runTest {
        ItemTestMiddleware(this).testWithInternalAndExternalState(this) {
            expectNoItems()
        }
    }

    @Test
    fun internal_expects_no_items_fails_when_there_are_unconsumed_items() = runTest {
        ItemTestMiddleware(this).testWithInternalState(
            testScope = this,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            assertFails { expectNoItems() }
        }
    }

    @Test
    fun external_expects_no_items_fails_when_there_are_unconsumed_items() = runTest {
        ItemTestMiddleware(this).testWithExternalState(
            testScope = this,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            assertFails { expectNoItems() }
        }
    }

    @Test
    fun internal_and_external_expects_no_items_fails_when_there_are_unconsumed_items() = runTest {
        ItemTestMiddleware(this).testWithInternalAndExternalState(
            testScope = this,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            // Under normal usage we wouldn't catch the failure; as we are we get a failure for each initial state, internal and external
            assertFails { expectNoItems() }
            assertFails { expectNoItems() }
        }
    }

    private inner class ItemTestMiddleware(scope: TestScope) :
        OrbitContainerHost<InternalState, ExternalState, Int> {
        override val container: OrbitContainer<InternalState, ExternalState, Int> = scope.backgroundScope.orbitContainer(
            initialState,
            ::transformState
        )

        private fun transformState(internalState: InternalState): ExternalState = ExternalState(internalState.count.toString())

        fun newState(action: Int) = intent {
            reduce {
                InternalState(count = action)
            }
        }

        fun newSideEffect(action: Int) = intent {
            postSideEffect(action)
        }
    }

    private data class InternalState(val count: Int = Random.nextInt())

    private data class ExternalState(val count: String)
}
