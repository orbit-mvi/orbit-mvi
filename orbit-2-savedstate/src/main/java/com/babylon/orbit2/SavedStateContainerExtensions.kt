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

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.babylon.orbit2.Container.Settings

internal val Container.Companion.SAVED_STATE_KEY
    get() = "state"

/**
 * Allows you to used the Android ViewModel's saved state support.
 *
 * Provide a [SavedStateHandle] in order for the state to be automatically saved as you use the
 * container. *This requires your state to be [Parcelable].*
 *
 *
 * @param initialState The initial state of the container.
 * @param savedStateHandle The [SavedStateHandle] corresponding to this host. Typically retrieved
 * from the containing [ViewModel]
 * @param settings The [Settings] to set the container up with.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner after the container has been interacted with in any way.
 * @return Default [Container] implementation
 */
fun <STATE : Any, SIDE_EFFECT : Any> Container.Companion.create(
    initialState: STATE,
    savedStateHandle: SavedStateHandle,
    settings: Settings = Settings(),
    onCreate: (() -> Unit)? = null
): Container<STATE, SIDE_EFFECT> {
    val savedState: STATE? = savedStateHandle[SAVED_STATE_KEY]

    val realContainer: Container<STATE, SIDE_EFFECT> =
        when {
            savedState != null -> create(savedState)
            else -> create(initialState, settings, onCreate)
        }
    return SavedStateContainerDecorator(
        realContainer,
        savedStateHandle
    )
}
