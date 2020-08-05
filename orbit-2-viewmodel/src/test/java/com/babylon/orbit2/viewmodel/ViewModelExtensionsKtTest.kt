/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2.viewmodel

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.appmattus.kotlinfixture.kotlinFixture
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.reduce
import com.babylon.orbit2.test
import kotlinx.android.parcel.Parcelize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ViewModelExtensionsKtTest {
    private val fixture = kotlinFixture()

    @Test
    fun `When saved state is present it is read`() {
        val initialState = fixture<TestState>()
        val savedState = fixture<TestState>()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState))

        val middleware = Middleware(savedStateHandle, initialState)

        assertThat(middleware.container.currentState).isEqualTo(savedState)
    }

    @Test
    fun `When saved state is not present the initial state is unchanged`() {
        val initialState = fixture<TestState>()
        val savedStateHandle = SavedStateHandle()

        val middleware = Middleware(savedStateHandle, initialState)

        assertThat(middleware.container.currentState).isEqualTo(initialState)
    }

    @Test
    fun `Modified state is saved in the saved state handle`() {
        val initialState = fixture<TestState>()
        val something = fixture<Int>()
        val savedStateHandle = SavedStateHandle()
        val middleware = Middleware(savedStateHandle, initialState)
        val testStateObserver = middleware.container.stateStream.test()

        middleware.something(something)

        testStateObserver.awaitCount(2)

        assertThat(savedStateHandle.get<TestState?>(SAVED_STATE_KEY)).isEqualTo(
            TestState(something)
        )
    }

    @Test
    fun `When saved state is present calls onCreate with true`() {
        val initialState = fixture<TestState>()
        val savedState = fixture<TestState>()
        val savedStateHandle = SavedStateHandle(mapOf(SAVED_STATE_KEY to savedState))
        var onCreateState: TestState? = null

        val middleware = Middleware(savedStateHandle, initialState) {
            onCreateState = it
        }

        // Used to trigger execution of onCreate
        middleware.container.stateStream.observe { }

        assertThat(onCreateState).isEqualTo(savedState)
    }

    @Test
    fun `When saved state is not present calls onCreate with false`() {
        val initialState = fixture<TestState>()
        val savedStateHandle = SavedStateHandle()
        var onCreateState: TestState? = null

        val middleware = Middleware(savedStateHandle, initialState) {
            onCreateState = it
        }

        // Used to trigger execution of onCreate
        middleware.container.stateStream.observe { }

        assertThat(onCreateState).isEqualTo(initialState)
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

        fun something(action: Int) = orbit {
            reduce {
                state.copy(id = action)
            }
        }
    }

    @Parcelize
    data class TestState(val id: Int) : Parcelable
}
