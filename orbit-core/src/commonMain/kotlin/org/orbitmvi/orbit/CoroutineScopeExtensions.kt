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

package org.orbitmvi.orbit

import kotlinx.coroutines.CoroutineScope
import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import org.orbitmvi.orbit.internal.RealContainer
import org.orbitmvi.orbit.internal.TestContainerDecorator
import org.orbitmvi.orbit.syntax.Syntax

/**
 * Helps create a concrete container in a standard way.
 *
 * @param initialState The initial state of the container.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner when the container is first interacted with in any way.
 * @return An [OrbitContainer] implementation
 */
public fun <STATE : Any, SIDE_EFFECT : Any> CoroutineScope.orbitContainer(
    initialState: STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<STATE, STATE, SIDE_EFFECT> {
    return orbitContainer(
        initialState = initialState,
        transformState = { it },
        buildSettings = buildSettings,
        onCreate = onCreate
    )
}

/**
 * Helps create a concrete container with external state transformation in a standard way.
 *
 * @param initialState The initial state of the container.
 * @param transformState The function that transforms the internal state to the external state.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner when the container is first interacted with in any way.
 * @return An [OrbitContainer] implementation
 */
public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> CoroutineScope.orbitContainer(
    initialState: INTERNAL_STATE,
    transformState: (INTERNAL_STATE) -> EXTERNAL_STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
    val realContainer = RealContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>(
        initialState = initialState,
        settings = SettingsBuilder().apply { buildSettings() }.settings,
        parentScope = this,
        transformState = transformState
    )
    return if (onCreate == null) {
        TestContainerDecorator(
            initialState,
            realContainer,
            transformState
        )
    } else {
        TestContainerDecorator(
            initialState,
            LazyCreateContainerDecorator(
                realContainer
            ) { Syntax(this).onCreate() },
            transformState
        )
    }
}

/**
 * Helps create a concrete container in a standard way.
 *
 * @param initialState The initial state of the container.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner when the container is first interacted with in any way.
 * @return An [OrbitContainer] implementation
 */
@Deprecated(
    "Use orbitContainer instead",
    ReplaceWith("orbitContainer(initialState, buildSettings, onCreate)")
)
public fun <STATE : Any, SIDE_EFFECT : Any> CoroutineScope.container(
    initialState: STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<STATE, STATE, SIDE_EFFECT> = orbitContainer(initialState, buildSettings, onCreate)

/**
 * Helps create a concrete container with external state transformation in a standard way.
 *
 * @param initialState The initial state of the container.
 * @param transformState The function that transforms the internal state to the external state.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner when the container is first interacted with in any way.
 * @return An [OrbitContainer] implementation
 */
@Deprecated(
    "Use orbitContainer instead",
    ReplaceWith("orbitContainer(initialState, transformState, buildSettings, onCreate)")
)
public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> CoroutineScope.container(
    initialState: INTERNAL_STATE,
    transformState: (INTERNAL_STATE) -> EXTERNAL_STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> =
    orbitContainer(initialState, transformState, buildSettings, onCreate)
