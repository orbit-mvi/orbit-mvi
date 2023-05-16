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
@file:Suppress("DEPRECATION")

package org.orbitmvi.orbit.viewmodel

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.SettingsBuilder
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax

internal const val SAVED_STATE_KEY = "state"

/**
 * Creates a container scoped with ViewModelScope.
 *
 * @param initialState The initial state of the container.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The intent to execute when the container is created
 * @return A [Container] implementation
 */
fun <STATE : Any, SIDE_EFFECT : Any> ViewModel.container(
    initialState: STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
): Container<STATE, SIDE_EFFECT> {
    return viewModelScope.container(initialState, buildSettings, onCreate)
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
 * @param onCreate The intent to execute when the container is created, provided with the default or recreated state
 * @return A [Container] implementation
 */
fun <STATE : Parcelable, SIDE_EFFECT : Any> ViewModel.container(
    initialState: STATE,
    savedStateHandle: SavedStateHandle,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
): Container<STATE, SIDE_EFFECT> {
    val savedState: STATE? = savedStateHandle[SAVED_STATE_KEY]
    val state = savedState ?: initialState

    val realContainer: Container<STATE, SIDE_EFFECT> =
        viewModelScope.container(state, buildSettings, onCreate)

    return SavedStateContainerDecorator(
        realContainer,
        savedStateHandle
    )
}
