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

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import org.junit.Test
import org.orbitmvi.orbit.OrbitContainerHost
import kotlin.random.Random
import kotlin.test.assertEquals

class OrbitContainerHostViewModelExtensionsKtTest {

    @Test
    fun when_saved_state_is_present_it_is_read() {
        val initialState = TestState()
        val savedState = TestState()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState))

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
    fun when_saved_state_is_present_it_is_read_and_transformed() {
        val initialState = TestState()
        val savedState = TestState()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState))

        val middleware = ExternalStateMiddleware(savedStateHandle, initialState)

        assertEquals(TestExternalState(savedState.id.toString()), middleware.container.externalStateFlow.value)
    }

    private class Middleware(
        savedStateHandle: SavedStateHandle,
        initialState: TestState
    ) : OrbitContainerHost<TestState, TestState, Int>, ViewModel() {
        override val container = orbitContainer(
            initialState = initialState,
            savedStateHandle = savedStateHandle
        )
    }

    private class ExternalStateMiddleware(
        savedStateHandle: SavedStateHandle,
        initialState: TestState
    ) : OrbitContainerHost<TestState, TestExternalState, Int>, ViewModel() {
        override val container = orbitContainer(
            initialState = initialState,
            savedStateHandle = savedStateHandle,
            transformState = { TestExternalState(it.id.toString()) }
        )
    }

    @Parcelize
    data class TestState(val id: Int = Random.nextInt()) : Parcelable

    data class TestExternalState(val id: String)

    companion object {
        private const val SAVED_STATE_KEY = "state"
    }
}
