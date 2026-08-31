/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.syntax.Syntax
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [createOrbitContainer] is the factory for containers that are not owned by an
 * `OrbitContainerHost`, so every view model here is a plain [ViewModel].
 */
@OptIn(OrbitInternal::class)
class ViewModelCreateOrbitContainerTest : RobolectricTest() {

    @Test
    fun container_is_created_with_the_initial_state() {
        val initialState = TestState()

        val viewModel = PlainViewModel(initialState)

        assertEquals(initialState, viewModel.container.stateFlow.value)
    }

    @Test
    fun container_is_scoped_to_the_view_model_scope() = runTest {
        val initialState = TestState()
        val viewModel = PlainViewModel(initialState)
        val something = Random.nextInt()

        viewModel.container.stateFlow.test {
            assertEquals(initialState, awaitItem())

            viewModel.something(something).join()

            assertEquals(TestState(something), awaitItem())
        }
    }

    @Test
    fun external_state_is_transformed() = runTest {
        val viewModel = ExternalStatePlainViewModel(TestState(42))

        assertEquals(TestState(42), viewModel.container.stateFlow.value)

        viewModel.container.externalStateFlow.test {
            assertEquals("42", awaitItem())
        }
    }

    @Test
    fun saved_state_is_restored_when_present() {
        val savedState = TestState()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KMP_KEY to savedState.asSavedState()))

        val viewModel = SavedStatePlainViewModel(savedStateHandle, TestState())

        assertEquals(savedState, viewModel.container.stateFlow.value)
    }

    @Test
    fun modified_state_is_written_back_to_the_saved_state_handle() = runTest {
        val initialState = TestState()
        val savedStateHandle = SavedStateHandle()
        val viewModel = SavedStatePlainViewModel(savedStateHandle, initialState)
        val something = Random.nextInt()

        viewModel.container.stateFlow.test {
            assertEquals(initialState, awaitItem())

            viewModel.something(something).join()

            assertEquals(TestState(something), awaitItem())
            assertEquals(
                TestState(something),
                savedStateHandle.get<SavedState>(SAVED_STATE_KMP_KEY)?.toTestState()
            )
        }
    }

    private class PlainViewModel(initialState: TestState) : ViewModel() {
        val container: OrbitContainer<TestState, TestState, Int> = createOrbitContainer(initialState)

        fun something(action: Int): Job = container.something(action)
    }

    private class ExternalStatePlainViewModel(initialState: TestState) : ViewModel() {
        val container: OrbitContainer<TestState, String, Int> =
            createOrbitContainer(initialState, transformState = { it.id.toString() })
    }

    private class SavedStatePlainViewModel(
        savedStateHandle: SavedStateHandle,
        initialState: TestState
    ) : ViewModel() {
        val container: OrbitContainer<TestState, TestState, Int> = createOrbitContainer(
            initialState = initialState,
            savedStateHandle = savedStateHandle,
            serializer = TestState.serializer()
        )

        fun something(action: Int): Job = container.something(action)
    }

    @Serializable
    data class TestState(val id: Int = Random.nextInt())

    private companion object {
        const val SAVED_STATE_KMP_KEY = "state-kmp"

        fun OrbitContainer<TestState, TestState, Int>.something(action: Int): Job = orbit {
            Syntax(this).reduce { state.copy(id = action) }
        }

        fun TestState.asSavedState(): SavedState = encodeToSavedState(TestState.serializer(), this)

        fun SavedState.toTestState(): TestState = decodeFromSavedState(TestState.serializer(), this)
    }
}
