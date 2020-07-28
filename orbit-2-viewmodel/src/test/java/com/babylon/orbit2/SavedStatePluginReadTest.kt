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

package com.babylon.orbit2

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.appmattus.kotlinfixture.kotlinFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SavedStatePluginReadTest {

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

    private data class TestState(val id: Int) : Parcelable {
        constructor(parcel: Parcel) : this(parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<TestState> {
            override fun createFromParcel(parcel: Parcel): TestState {
                return TestState(parcel)
            }

            override fun newArray(size: Int): Array<TestState?> {
                return arrayOfNulls(size)
            }
        }
    }

    private inner class Middleware(
        savedStateHandle: SavedStateHandle,
        initialState: TestState
    ) : ContainerHost<TestState, Int>, ViewModel() {
        override val container = container<TestState, Int>(
            initialState,
            savedStateHandle
        )
    }
}
