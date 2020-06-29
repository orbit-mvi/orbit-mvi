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

/**
 * Apply this interface to anything you want to become an orbit container host.
 * Typically this will be an Android ViewModel but it can be applied to simple presenters etc.
 *
 */
interface ContainerHost<STATE : Any, SIDE_EFFECT : Any> {
    /**
     * The orbit [Container] instance.
     *
     * Use the [Container.create] factory functions to easily obtain a [Container].
     *
     * ```
     * override val container = Container.create<MyState, MySideEffect>(initialState)
     * ```
     */
    val container: Container<STATE, SIDE_EFFECT>

    /**
     * Build and execute an orbit flow on [container] using the [Builder] and
     * associated DSL functions.
     *
     * @param init lambda returning the operator chain that represents the flow
     */
    @Orbit2Dsl
    fun orbit(init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>) =
        container.orbit(init)
}
