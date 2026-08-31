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

package org.orbitmvi.orbit

import kotlinx.coroutines.CoroutineScope
import org.orbitmvi.orbit.syntax.Syntax

/**
 * Helps create a concrete container in a standard way.
 *
 * The state and side effect types are inferred from the [OrbitContainerHost] this container belongs
 * to, so they do not need to be repeated at the call site.
 *
 * ```
 * class MyViewModel(scope: CoroutineScope) : OrbitContainerHost<MyState, MyState, MySideEffect> {
 *     override val container = orbitContainer(scope, MyState())
 * }
 * ```
 *
 * @param scope The [CoroutineScope] the container is scoped to.
 * @param initialState The initial state of the container.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner when the container is first interacted with in any way.
 * @return An [OrbitContainer] implementation
 */
public fun <STATE : Any, SIDE_EFFECT : Any> OrbitContainerHost<STATE, STATE, SIDE_EFFECT>.orbitContainer(
    scope: CoroutineScope,
    initialState: STATE,
    buildSettings: SettingsBuilder.() -> Unit = {},
    onCreate: (suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
): OrbitContainer<STATE, STATE, SIDE_EFFECT> = scope.createOrbitContainer(initialState, buildSettings, onCreate)

/**
 * Helps create a concrete container with external state transformation in a standard way.
 *
 * The state and side effect types are inferred from the [OrbitContainerHost] this container belongs
 * to, so they do not need to be repeated at the call site.
 *
 * ```
 * class MyViewModel(scope: CoroutineScope) : OrbitContainerHost<InternalState, ExternalState, MySideEffect> {
 *     override val container = orbitContainer(scope, InternalState(), ::transformState)
 * }
 * ```
 *
 * @param scope The [CoroutineScope] the container is scoped to.
 * @param initialState The initial state of the container.
 * @param transformState The function that transforms the internal state to the external state.
 * @param buildSettings This builder can be used to change the container's settings.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner when the container is first interacted with in any way.
 * @return An [OrbitContainer] implementation
 */
public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any>
    OrbitContainerHost<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>.orbitContainer(
        scope: CoroutineScope,
        initialState: INTERNAL_STATE,
        transformState: (INTERNAL_STATE) -> EXTERNAL_STATE,
        buildSettings: SettingsBuilder.() -> Unit = {},
        onCreate: (suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit)? = null
    ): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> =
    scope.createOrbitContainer(initialState, transformState, buildSettings, onCreate)
