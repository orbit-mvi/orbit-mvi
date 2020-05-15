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
import com.appmattus.kotlinfixture.kotlinFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SavedStatePluginWriteTest {

    private val fixture = kotlinFixture()

    @Test
    fun `Modified state is saved in the saved state handle`() {
        val initialState = fixture<TestState>()
        val something = fixture<Int>()
        val savedStateHandle = SavedStateHandle()
        val middleware = Middleware(savedStateHandle, initialState)
        val testStateObserver = middleware.container.orbit.test()

        middleware.something(something)

        testStateObserver.awaitCount(2)

        assertThat(savedStateHandle.get<TestState?>(Container.SAVED_STATE_KEY)).isEqualTo(
            TestState(something)
        )
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
                return TestState(
                    parcel
                )
            }

            override fun newArray(size: Int): Array<TestState?> {
                return arrayOfNulls(size)
            }
        }
    }

    private inner class Middleware(
        savedStateHandle: SavedStateHandle,
        initialState: TestState
    ) : Host<TestState, Int> {
        override val container: Container<TestState, Int> = Container.create(
            initialState,
            savedStateHandle
        )

        fun something(action: Int) = orbit(action) {
            reduce {
                state.copy(id = event)
            }
        }
    }
}
