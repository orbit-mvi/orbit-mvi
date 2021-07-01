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

package org.orbitmvi.orbit.viewmodel

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.test
import kotlin.random.Random
import kotlin.test.assertEquals
import org.junit.Test

class ViewModelExtensionsKtTest {
    @Test
    fun `When saved state is present it is read`() {
        val initialState = TestState()
        val savedState = TestState()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState))

        val middleware = Middleware(savedStateHandle, initialState)

        assertEquals(savedState, middleware.container.stateFlow.value)
    }

    @Test
    fun `When saved state is not present the initial state is unchanged`() {
        val initialState = TestState()
        val savedStateHandle = SavedStateHandle()

        val middleware = Middleware(savedStateHandle, initialState)

        assertEquals(initialState, middleware.container.stateFlow.value)
    }

    @Test
    fun `Modified state is saved in the saved state handle for stateFlow`() {
        val initialState = TestState()
        val something = Random.nextInt()
        val savedStateHandle = SavedStateHandle()
        val middleware = Middleware(savedStateHandle, initialState)
        val testStateObserver = middleware.container.stateFlow.test()

        middleware.something(something)

        testStateObserver.awaitCount(2)

        assertEquals(TestState(something), savedStateHandle.get<TestState?>(SAVED_STATE_KEY))
    }

    @Test
    fun `When saved state is present calls onCreate with true`() {
        val initialState = TestState()
        val savedState = TestState()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState))
        var onCreateState: TestState? = null

        val middleware = Middleware(savedStateHandle, initialState) {
            onCreateState = it
        }

        // Used to trigger execution of onCreate
        middleware.container.stateFlow.test().awaitCount(1)

        assertEquals(savedState, onCreateState)
    }

    @Test
    fun `When saved state is not present calls onCreate with false`() {
        val initialState = TestState()
        val savedStateHandle = SavedStateHandle()
        var onCreateState: TestState? = null

        val middleware = Middleware(savedStateHandle, initialState) {
            onCreateState = it
        }

        // Used to trigger execution of onCreate
        middleware.container.stateFlow.test().awaitCount(1)

        assertEquals(initialState, onCreateState)
    }

    private class Middleware(
        savedStateHandle: SavedStateHandle,
        initialState: TestState,
        onCreate: ((TestState) -> Unit)? = null
    ) : ContainerHost<TestState, Int>, ViewModel() {
        override val container = container<TestState, Int>(
            initialState = initialState,
            savedStateHandle = savedStateHandle,
            onCreate = onCreate
        )

        fun something(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }
    }

    @Parcelize
    data class TestState(val id: Int = Random.nextInt()) : Parcelable
}
