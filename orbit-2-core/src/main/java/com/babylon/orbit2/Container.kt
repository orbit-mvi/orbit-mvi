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

import com.babylon.orbit2.idling.IdlingResource
import com.babylon.orbit2.idling.NoopIdlingResource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

/**
 * The heart of the Orbit MVI system. Represents an MVI container with its input and outputs.
 * You can manipulate the container through the [orbit] function
 *
 * @param STATE The container's state type.
 * @param SIDE_EFFECT The type of side effects posted by this container. Can be [Nothing] if this
 * container never posts side effects.
 */
interface Container<STATE : Any, SIDE_EFFECT : Any> {
    /**
     * The container's current state.
     */
    val currentState: STATE

    /**
     * A [Flow] of state updates. Emits the latest state upon subscription and serves only distinct
     * values (through equality comparison).
     */
    val stateFlow: Flow<STATE>

    /**
     * A [Flow] of one-off side effects posted from [Builder.sideEffect]. Caches side effects when there are no collectors.
     * The size of the cache can be controlled via Container [Settings] and determines if and when the orbit thread suspends when you
     * post a side effect. The default is unlimited. You don't have to touch this unless you are posting many side effects which could result in
     * [OutOfMemoryError].
     *
     * This is designed to be collected by one observer only in order to ensure that side effect caching works in a predictable way.
     * If your particular use case requires multi-casting use `broadcast` on this [Flow], but be aware that caching will not work for the
     * resulting `BroadcastChannel`.
     */
    val sideEffectFlow: Flow<SIDE_EFFECT>

    /**
     * A [Stream] of state updates. Emits the latest state upon subscription and serves only distinct
     * values (only changed states are emitted) by default.
     */
    @Suppress("DEPRECATION")
    @Deprecated("stateStream is deprecated and will be removed in Orbit 1.2.0, use stateFlow instead")
    val stateStream: Stream<STATE>

    /**
     * A [Stream] of one-off side effects posted from [Builder.sideEffect].
     * Depending on the [Settings] this container has been instantiated with, can support
     * side effect caching when there are no listeners (default).
     */
    @Suppress("DEPRECATION")
    @Deprecated("sideEffectStream is deprecated and will be removed in Orbit 1.2.0, use sideEffectFlow instead")
    val sideEffectStream: Stream<SIDE_EFFECT>

    /**
     * Builds and executes an orbit flow using the [Builder] and
     * associated DSL functions.
     *
     * @param init lambda returning the operator chain that represents the flow
     */
    fun orbit(
        init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>
    )

    /**
     * Represents additional settings to create the container with.
     *
     * @property sideEffectBufferSize Defines how many side effects can be buffered before the container suspends. If you are
     * sending many side effects and getting out of memory exceptions this can be turned down to suspend the container instead.
     * Unlimited by default.
     * @property idlingRegistry The registry used by the container for signalling idling for UI tests
     */
    class Settings(
        val sideEffectBufferSize: Int = Channel.UNLIMITED,
        val idlingRegistry: IdlingResource = NoopIdlingResource()
    )
}
