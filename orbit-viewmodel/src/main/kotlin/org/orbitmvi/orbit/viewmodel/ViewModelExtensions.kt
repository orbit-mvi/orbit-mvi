/*
 * Copyright 2021-2023 Mikołaj Leszczyński & Appmattus Limited
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
@file:Suppress("DEPRECATION")

package org.orbitmvi.orbit.viewmodel

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.SettingsBuilder
import org.orbitmvi.orbit.container

internal const val SAVED_STATE_KEY = "state"

/**
 * Creates a container scoped with ViewModelScope.
 *
 * @param initialState The initial state of the container.
 * @param settings The [Container.Settings] to set the container up with.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner after the container has been interacted with in any way.
 * @return A [Container] implementation
 */
@Deprecated(message = "Use overload with settings builder instead. This will be removed in the future.")
fun <STATE : Any, SIDE_EFFECT : Any> ViewModel.container(
    initialState: STATE,
    settings: Container.Settings,
    onCreate: ((state: STATE) -> Unit)? = null
): Container<STATE, SIDE_EFFECT> {
    return viewModelScope.container(initialState, settings, onCreate)
}

/**
 * Creates a container scoped with ViewModelScope and allows you to used the
 * Android ViewModel's saved state support.
 *
 * Provide a [SavedStateHandle] in order for your [Parcelable] state to be automatically saved as
 * you use the container.
 *
 * @param initialState The initial state of the container.
 * @param savedStateHandle The [SavedStateHandle] corresponding to this host. Typically retrieved
 * from the containing [ViewModel]
 * @param settings The [Container.Settings] to set the container up with.
 * @param onCreate The lambda to execute when the container is created, parameter is false, or
 * recreated, parameter is true. By default it is executed in a lazy manner after the container
 * has been interacted with in any way.
 * @return A [Container] implementation
 */
@Deprecated(message = "Use overload with settings builder instead. This will be removed in the future.")
fun <STATE : Parcelable, SIDE_EFFECT : Any> ViewModel.container(
    initialState: STATE,
    savedStateHandle: SavedStateHandle,
    settings: Container.Settings,
    onCreate: ((state: STATE) -> Unit)? = null
): Container<STATE, SIDE_EFFECT> {
    val savedState: STATE? = savedStateHandle[SAVED_STATE_KEY]
    val state = savedState ?: initialState

    val realContainer: Container<STATE, SIDE_EFFECT> =
        viewModelScope.container(state, settings, onCreate)

    return SavedStateContainerDecorator(
        realContainer,
        savedStateHandle
    )
}

/**
 * Creates a container scoped with ViewModelScope.
 *
 * @param initialState The initial state of the container.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner after the container has been interacted with in any way.
 * @return A [Container] implementation
 */
fun <STATE : Any, SIDE_EFFECT : Any> ViewModel.container(
    initialState: STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    containerHostName: String? = null,
    onCreate: ((state: STATE) -> Unit)? = null
): Container<STATE, SIDE_EFFECT> {
    return viewModelScope.container(initialState, buildSettings, containerHostName, onCreate)
}

/**
 * Creates a container scoped with ViewModelScope and allows you to used the
 * Android ViewModel's saved state support.
 *
 * Provide a [SavedStateHandle] in order for your [Parcelable] state to be automatically saved as
 * you use the container.
 *
 * @param initialState The initial state of the container.
 * @param savedStateHandle The [SavedStateHandle] corresponding to this host. Typically retrieved
 * from the containing [ViewModel]
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The lambda to execute when the container is created, parameter is false, or
 * recreated, parameter is true. By default it is executed in a lazy manner after the container
 * has been interacted with in any way.
 * @return A [Container] implementation
 */
fun <STATE : Parcelable, SIDE_EFFECT : Any> ViewModel.container(
    initialState: STATE,
    savedStateHandle: SavedStateHandle,
    buildSettings: SettingsBuilder.() -> Unit = {},
    containerHostName: String? = null,
    onCreate: ((state: STATE) -> Unit)? = null
): Container<STATE, SIDE_EFFECT> {
    val savedState: STATE? = savedStateHandle[SAVED_STATE_KEY]
    val state = savedState ?: initialState

    val realContainer: Container<STATE, SIDE_EFFECT> =
        viewModelScope.container(state, buildSettings, containerHostName, onCreate)

    return SavedStateContainerDecorator(
        realContainer,
        savedStateHandle
    )
}
