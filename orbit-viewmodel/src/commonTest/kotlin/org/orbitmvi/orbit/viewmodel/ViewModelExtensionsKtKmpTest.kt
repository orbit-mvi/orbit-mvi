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

package org.orbitmvi.orbit.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.test.test
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewModelExtensionsKtKmpTest : RobolectricTest() {

    @Test
    fun when_saved_state_is_present_it_is_read() {
        val initialState = TestState()
        val savedState = TestState()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState.asSavedState()))

        val middleware = Middleware(savedStateHandle, initialState)

        assertEquals(savedState, middleware.container.stateFlow.value)
    }

    @Test
    fun when_saved_state_is_not_present_the_initial_state_is_unchanged() {
        val initialState = TestState()
        val savedStateHandle = SavedStateHandle()

        val middleware = Middleware(savedStateHandle, initialState)

        assertEquals(initialState, middleware.container.stateFlow.value)
    }

    @Test
    fun modified_state_is_saved_in_the_saved_state_handle_for_state_flow() = runTest {
        val initialState = TestState()
        val something = Random.nextInt()
        val savedStateHandle = SavedStateHandle()
        val middleware = Middleware(savedStateHandle, initialState)

        middleware.container.stateFlow.test {
            assertEquals(initialState, awaitItem())

            middleware.something(something).join()

            assertEquals(TestState(something), awaitItem())
            assertEquals(
                TestState(something),
                savedStateHandle.get<SavedState>(SAVED_STATE_KEY)?.toTestState()
            )
        }
    }

    @Test
    fun modified_state_is_saved_in_the_saved_state_handle_for_ref_count_state_flow() = runTest {
        val initialState = TestState()
        val something = Random.nextInt()
        val savedStateHandle = SavedStateHandle()
        val middleware = Middleware(savedStateHandle, initialState)
        middleware.container.refCountStateFlow.test {
            assertEquals(initialState, awaitItem())

            middleware.something(something).join()

            assertEquals(TestState(something), awaitItem())
            assertEquals(
                TestState(something),
                savedStateHandle.get<SavedState>(SAVED_STATE_KEY)?.toTestState()
            )
        }
    }

    @Test
    fun when_saved_state_is_present_calls_on_create_with_restored_state() = runTest {
        val initialState = TestState()
        val savedState = TestState()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState.asSavedState()))

        Middleware(savedStateHandle, initialState) {
            assertEquals(savedState, state)
        }.test(this) {
            runOnCreate()
        }
    }

    @Test
    fun when_saved_state_is_not_present_calls_on_create_with_initial_state() = runTest {
        val initialState = TestState()
        val savedStateHandle = SavedStateHandle()

        Middleware(savedStateHandle, initialState) {
            assertEquals(initialState, state)
        }.test(this) {
            runOnCreate()
        }
    }

    private class Middleware(
        savedStateHandle: SavedStateHandle,
        initialState: TestState,
        onCreate: (suspend Syntax<TestState, Int>.() -> Unit)? = null
    ) : ContainerHost<TestState, Int>, ViewModel() {
        override val container = container(
            initialState = initialState,
            savedStateHandle = savedStateHandle,
            serializer = TestState.serializer(),
            onCreate = onCreate
        )

        fun something(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }
    }

    @Serializable
    data class TestState(val id: Int = Random.nextInt()) {
        fun asSavedState(): SavedState = encodeToSavedState(serializer(), this)
    }

    private fun SavedState.toTestState(): TestState =
        decodeFromSavedState(TestState.serializer(), this)

    companion object {
        private const val SAVED_STATE_KEY = "state-kmp"
    }
}
