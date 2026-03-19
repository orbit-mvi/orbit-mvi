/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import kotlinx.serialization.KSerializer
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.SettingsBuilder
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.Syntax

private const val SAVED_STATE_KMP_KEY = "state-kmp"

/**
 * Creates a container scoped with ViewModelScope.
 *
 * @param initialState The initial state of the container.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The intent to execute when the container is created
 * @return An [OrbitContainer] implementation
 */
public fun <STATE : Any, SIDE_EFFECT : Any> ViewModel.container(
    initialState: STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<STATE, STATE, SIDE_EFFECT> {
    return viewModelScope.container(initialState, buildSettings, onCreate)
}

/**
 * Creates a container scoped with ViewModelScope with external state transformation.
 *
 * @param initialState The initial state of the container.
 * @param transformState The function that transforms the internal state to the external state.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The intent to execute when the container is created
 * @return An [OrbitContainer] implementation
 */
public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> ViewModel.container(
    initialState: INTERNAL_STATE,
    transformState: (INTERNAL_STATE) -> EXTERNAL_STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
    return viewModelScope.container(initialState, transformState, buildSettings, onCreate)
}

/**
 * Creates a container scoped with ViewModelScope with saved state support.
 *
 * Provide a [SavedStateHandle] in order for your state to be automatically saved as
 * you use the container.
 *
 * @param initialState The initial state of the container.
 * @param savedStateHandle The [SavedStateHandle] corresponding to this host. Typically retrieved
 * from the containing [ViewModel]
 * @param serializer The [KSerializer] to use for serializing and deserializing state.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The intent to execute when the container is created
 * @return An [OrbitContainer] implementation
 */
public fun <STATE : Any, SIDE_EFFECT : Any> ViewModel.container(
    initialState: STATE,
    savedStateHandle: SavedStateHandle,
    serializer: KSerializer<STATE>,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<STATE, STATE, SIDE_EFFECT> {
    return container(
        initialState = initialState,
        savedStateHandle = savedStateHandle,
        serializer = serializer,
        transformState = { it },
        buildSettings = buildSettings,
        onCreate = onCreate
    )
}

/**
 * Creates a container scoped with ViewModelScope with external state transformation and
 * saved state support.
 *
 * Provide a [SavedStateHandle] in order for your state to be automatically saved as
 * you use the container.
 *
 * @param initialState The initial state of the container.
 * @param savedStateHandle The [SavedStateHandle] corresponding to this host.
 * @param serializer The [KSerializer] to use for serializing and deserializing state.
 * @param transformState The function that transforms the internal state to the external state.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The intent to execute when the container is created
 * @return An [OrbitContainer] implementation
 */
@Suppress("LongParameterList")
public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> ViewModel.container(
    initialState: INTERNAL_STATE,
    savedStateHandle: SavedStateHandle,
    serializer: KSerializer<INTERNAL_STATE>,
    transformState: (INTERNAL_STATE) -> EXTERNAL_STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
    val savedState: INTERNAL_STATE? =
        savedStateHandle.get<SavedState>(SAVED_STATE_KMP_KEY)?.let {
            decodeFromSavedState(
                deserializer = serializer,
                savedState = it
            )
        }

    val state = savedState ?: initialState

    val realContainer: OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> =
        viewModelScope.container(state, transformState, buildSettings, onCreate)

    return SavedStateContainerDecorator(
        realContainer,
        savedStateHandle,
        serializer,
        SAVED_STATE_KMP_KEY
    )
}
