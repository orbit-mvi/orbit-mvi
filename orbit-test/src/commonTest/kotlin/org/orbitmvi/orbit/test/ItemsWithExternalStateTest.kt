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
import org.orbitmvi.orbit.ContainerHostWithExternalState
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.withExternalState
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ItemsWithExternalStateTest {

    private val initialState = InternalState()

    @Test
    fun internal_items_can_be_skipped() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
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

        ItemTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
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

        ItemTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
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

        ItemTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            assertEquals(ItemWithExternalState.InternalStateItem(InternalState(1)), awaitItem())
            assertEquals(ItemWithExternalState.SideEffectItem(3), awaitItem())
            assertEquals(ItemWithExternalState.InternalStateItem(InternalState(2)), awaitItem())
            assertEquals(ItemWithExternalState.SideEffectItem(4), awaitItem())
        }
    }

    @Test
    fun external_items_can_be_retrieved() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            assertEquals(ItemWithExternalState.ExternalStateItem(ExternalState("1")), awaitItem())
            assertEquals(ItemWithExternalState.SideEffectItem(3), awaitItem())
            assertEquals(ItemWithExternalState.ExternalStateItem(ExternalState("2")), awaitItem())
            assertEquals(ItemWithExternalState.SideEffectItem(4), awaitItem())
        }
    }

    @Test
    fun internal_and_external_items_can_be_retrieved() = runTest {
        val state1 = 1
        val state2 = 2
        val sideEffect1 = 3
        val sideEffect2 = 4

        ItemTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
            containerHost.newState(state1)
            containerHost.newSideEffect(sideEffect1)
            containerHost.newState(state2)
            containerHost.newSideEffect(sideEffect2)

            assertEquals(ItemWithExternalState.InternalStateItem(InternalState(1)), awaitItem())
            assertEquals(ItemWithExternalState.ExternalStateItem(ExternalState("1")), awaitItem())
            assertEquals(ItemWithExternalState.SideEffectItem(3), awaitItem())
            assertEquals(ItemWithExternalState.InternalStateItem(InternalState(2)), awaitItem())
            assertEquals(ItemWithExternalState.ExternalStateItem(ExternalState("2")), awaitItem())
            assertEquals(ItemWithExternalState.SideEffectItem(4), awaitItem())
        }
    }

    @Test
    fun internal_correctly_expects_no_items() = runTest {
        ItemTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_ONLY)) {
            expectNoItems()
        }
    }

    @Test
    fun external_correctly_expects_no_items() = runTest {
        ItemTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.EXTERNAL_ONLY)) {
            expectNoItems()
        }
    }

    @Test
    fun internal_and_external_correctly_expects_no_items() = runTest {
        ItemTestMiddleware(this).test(this, settings = TestSettings(awaitState = AwaitState.INTERNAL_AND_EXTERNAL)) {
            expectNoItems()
        }
    }

    @Test
    fun internal_expects_no_items_fails_when_there_are_unconsumed_items() = runTest {
        ItemTestMiddleware(this).test(
            testScope = this,
            settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.INTERNAL_ONLY)
        ) {
            assertFails { expectNoItems() }
        }
    }

    @Test
    fun external_expects_no_items_fails_when_there_are_unconsumed_items() = runTest {
        ItemTestMiddleware(this).test(
            testScope = this,
            settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.EXTERNAL_ONLY)
        ) {
            assertFails { expectNoItems() }
        }
    }

    @Test
    fun internal_and_external_expects_no_items_fails_when_there_are_unconsumed_items() = runTest {
        ItemTestMiddleware(this).test(
            testScope = this,
            settings = TestSettings(autoCheckInitialState = false, awaitState = AwaitState.INTERNAL_AND_EXTERNAL)
        ) {
            // Under normal usage we wouldn't catch the failure; as we are we get a failure for each initial state, internal and external
            assertFails { expectNoItems() }
            assertFails { expectNoItems() }
        }
    }

    private inner class ItemTestMiddleware(scope: TestScope) :
        ContainerHostWithExternalState<InternalState, ExternalState, Int> {
        override val container = scope.backgroundScope.container<InternalState, Int>(initialState).withExternalState(::transformState)

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
