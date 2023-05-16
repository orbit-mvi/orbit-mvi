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

package org.orbitmvi.orbit

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.ContainerContext

/**
 * The heart of the Orbit MVI system. Represents an MVI container with its input and outputs.
 * You can manipulate the container through the [orbit] function
 *
 * @param STATE The container's state type.
 * @param SIDE_EFFECT The type of side effects posted by this container. Can be [Nothing] if this
 * container never posts side effects.
 */
public interface Container<STATE : Any, SIDE_EFFECT : Any> {

    /**
     * Settings that the container was set up with
     */
    public val settings: RealSettings

    /**
     * A [StateFlow] of state updates. Emits the latest state upon subscription and serves only distinct
     * values (through equality comparison).
     */
    public val stateFlow: StateFlow<STATE>

    /**
     * A [Flow] of one-off side effects posted from [Container]. Caches side effects when there are no collectors.
     * The size of the cache can be controlled via [SettingsBuilder] and determines if and when the orbit thread suspends when you
     * post a side effect. The default is unlimited. You don't have to touch this unless you are posting many side effects which could result in
     * `OutOfMemoryError`.
     *
     * This is designed to be collected by one observer only in order to ensure that side effect caching works in a predictable way.
     * If your particular use case requires multi-casting use `broadcast` on this [Flow], but be aware that caching will not work for the
     * resulting `BroadcastChannel`.
     */
    public val sideEffectFlow: Flow<SIDE_EFFECT>

    /**
     * Executes an orbit intent. The intents are built in the [ContainerHost] using your chosen syntax.
     *
     * @param orbitIntent lambda returning the suspend function representing the intent
     */
    public suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit): Job

    /**
     * Executes an orbit intent inline, circumventing orbit's dispatching. The intents are built in the [ContainerHost] using your chosen syntax.
     *
     * @param orbitIntent lambda returning the suspend function representing the intent
     */
    @OrbitExperimental
    public suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit)

    /**
     * Cancels the container scope and all intents in progress.
     */
    @OrbitExperimental
    public fun cancel()

    /**
     * Joins all in progress intents in the container. This suspends until all intents have completed.
     */
    @OrbitExperimental
    public suspend fun joinIntents()
}
