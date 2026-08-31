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
import androidx.lifecycle.viewModelScope
import kotlinx.serialization.KSerializer
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.SettingsBuilder
import org.orbitmvi.orbit.orbitContainer
import org.orbitmvi.orbit.syntax.Syntax
import kotlin.jvm.JvmName

/**
 * Creates a container scoped with ViewModelScope.
 *
 * This overload applies when the [ViewModel] also implements [OrbitContainerHost]. The state and
 * side effect types are inferred from the host declaration, so they do not need to be repeated at
 * the call site.
 *
 * @param initialState The initial state of the container.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The intent to execute when the container is created
 * @return An [OrbitContainer] implementation
 */
@JvmName("containerHostOrbitContainer")
public fun <T, STATE : Any, SIDE_EFFECT : Any> T.orbitContainer(
    initialState: STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<STATE, STATE, SIDE_EFFECT> where T : OrbitContainerHost<STATE, STATE, SIDE_EFFECT>, T : ViewModel {
    return orbitContainer(viewModelScope, initialState, buildSettings, onCreate)
}

/**
 * Creates a container scoped with ViewModelScope with external state transformation.
 *
 * This overload applies when the [ViewModel] also implements [OrbitContainerHost]. The state and
 * side effect types are inferred from the host declaration, so they do not need to be repeated at
 * the call site.
 *
 * @param initialState The initial state of the container.
 * @param transformState The function that transforms the internal state to the external state.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The intent to execute when the container is created
 * @return An [OrbitContainer] implementation
 */
@JvmName("containerHostOrbitContainer")
public fun <T, INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> T.orbitContainer(
    initialState: INTERNAL_STATE,
    transformState: (INTERNAL_STATE) -> EXTERNAL_STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>
    where T : OrbitContainerHost<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>, T : ViewModel {
    return orbitContainer(viewModelScope, initialState, transformState, buildSettings, onCreate)
}

/**
 * Creates a container scoped with ViewModelScope with saved state support.
 *
 * This overload applies when the [ViewModel] also implements [OrbitContainerHost]. The state and
 * side effect types are inferred from the host declaration, so they do not need to be repeated at
 * the call site.
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
@JvmName("containerHostOrbitContainer")
public fun <T, STATE : Any, SIDE_EFFECT : Any> T.orbitContainer(
    initialState: STATE,
    savedStateHandle: SavedStateHandle,
    serializer: KSerializer<STATE>,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<STATE, STATE, SIDE_EFFECT> where T : OrbitContainerHost<STATE, STATE, SIDE_EFFECT>, T : ViewModel {
    return orbitContainer<STATE, SIDE_EFFECT>(
        initialState = initialState,
        savedStateHandle = savedStateHandle,
        serializer = serializer,
        buildSettings = buildSettings,
        onCreate = onCreate
    )
}

/**
 * Creates a container scoped with ViewModelScope with external state transformation and
 * saved state support.
 *
 * This overload applies when the [ViewModel] also implements [OrbitContainerHost]. The state and
 * side effect types are inferred from the host declaration, so they do not need to be repeated at
 * the call site.
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
@JvmName("containerHostOrbitContainer")
public fun <T, INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> T.orbitContainer(
    initialState: INTERNAL_STATE,
    savedStateHandle: SavedStateHandle,
    serializer: KSerializer<INTERNAL_STATE>,
    transformState: (INTERNAL_STATE) -> EXTERNAL_STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>
    where T : OrbitContainerHost<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>, T : ViewModel {
    return orbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>(
        initialState = initialState,
        savedStateHandle = savedStateHandle,
        serializer = serializer,
        transformState = transformState,
        buildSettings = buildSettings,
        onCreate = onCreate
    )
}
