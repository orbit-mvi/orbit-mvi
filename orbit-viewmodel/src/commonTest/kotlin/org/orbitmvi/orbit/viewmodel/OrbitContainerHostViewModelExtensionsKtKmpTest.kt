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
import androidx.savedstate.serialization.encodeToSavedState
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.test.testWithInternalState
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class OrbitContainerHostViewModelExtensionsKtKmpTest : RobolectricTest() {

    @Test
    fun the_container_exposes_the_initial_state() {
        val initialState = TestState()

        val middleware = Middleware(initialState)

        assertEquals(initialState, middleware.container.stateFlow.value)
    }

    @Test
    fun intents_modify_the_state() = runTest {
        val initialState = TestState()
        val something = Random.nextInt()
        val middleware = Middleware(initialState)

        middleware.container.stateFlow.test {
            assertEquals(initialState, awaitItem())

            middleware.something(something).join()

            assertEquals(TestState(something), awaitItem())
        }
    }

    @Test
    fun on_create_is_called_with_the_initial_state() = runTest {
        val initialState = TestState()

        Middleware(initialState) {
            assertEquals(initialState, state)
        }.testWithInternalState {
            runOnCreate()
        }
    }

    @Test
    fun the_container_exposes_the_transformed_initial_state() {
        val initialState = TestState()

        val middleware = ExternalStateMiddleware(initialState)

        assertEquals(TestExternalState(initialState.id.toString()), middleware.container.externalStateFlow.value)
    }

    @Test
    fun intents_modify_the_transformed_state() = runTest {
        val initialState = TestState()
        val something = Random.nextInt()
        val middleware = ExternalStateMiddleware(initialState)

        middleware.container.externalStateFlow.test {
            assertEquals(TestExternalState(initialState.id.toString()), awaitItem())

            middleware.something(something).join()

            assertEquals(TestExternalState(something.toString()), awaitItem())
        }
    }

    @Test
    fun when_saved_state_is_present_it_is_read() {
        val initialState = TestState()
        val savedState = TestState()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState.asSavedState()))

        val middleware = SavedStateMiddleware(savedStateHandle, initialState)

        assertEquals(savedState, middleware.container.stateFlow.value)
    }

    @Test
    fun when_saved_state_is_not_present_the_initial_state_is_unchanged() {
        val initialState = TestState()
        val savedStateHandle = SavedStateHandle()

        val middleware = SavedStateMiddleware(savedStateHandle, initialState)

        assertEquals(initialState, middleware.container.stateFlow.value)
    }

    @Test
    fun when_saved_state_is_present_it_is_read_and_transformed() {
        val initialState = TestState()
        val savedState = TestState()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState.asSavedState()))

        val middleware = SavedStateExternalStateMiddleware(savedStateHandle, initialState)

        assertEquals(TestExternalState(savedState.id.toString()), middleware.container.externalStateFlow.value)
    }

    private class Middleware(
        initialState: TestState,
        onCreate: (suspend Syntax<TestState, Int>.() -> Unit)? = null
    ) : OrbitContainerHost<TestState, TestState, Int>, ViewModel() {
        override val container = orbitContainer(
            initialState = initialState,
            onCreate = onCreate
        )

        fun something(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }
    }

    private class ExternalStateMiddleware(
        initialState: TestState
    ) : OrbitContainerHost<TestState, TestExternalState, Int>, ViewModel() {
        override val container = orbitContainer(
            initialState = initialState,
            transformState = { TestExternalState(it.id.toString()) }
        )

        fun something(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }
    }

    private class SavedStateMiddleware(
        savedStateHandle: SavedStateHandle,
        initialState: TestState
    ) : OrbitContainerHost<TestState, TestState, Int>, ViewModel() {
        override val container = orbitContainer(
            initialState = initialState,
            savedStateHandle = savedStateHandle,
            serializer = TestState.serializer()
        )
    }

    private class SavedStateExternalStateMiddleware(
        savedStateHandle: SavedStateHandle,
        initialState: TestState
    ) : OrbitContainerHost<TestState, TestExternalState, Int>, ViewModel() {
        override val container = orbitContainer(
            initialState = initialState,
            savedStateHandle = savedStateHandle,
            serializer = TestState.serializer(),
            transformState = { TestExternalState(it.id.toString()) }
        )
    }

    @Serializable
    data class TestState(val id: Int = Random.nextInt()) {
        fun asSavedState(): SavedState = encodeToSavedState(serializer(), this)
    }

    data class TestExternalState(val id: String)

    companion object {
        private const val SAVED_STATE_KEY = "state-kmp"
    }
}
