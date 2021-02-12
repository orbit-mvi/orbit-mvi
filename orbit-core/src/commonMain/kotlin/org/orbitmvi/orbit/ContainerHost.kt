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

/**
 * Apply this interface to anything you want to become an orbit container host.
 * Typically this will be an Android ViewModel but it can be applied to simple presenters etc.
 *
 * Extension functions `intent` and `orbit` are provided as a convenient way of launching orbit
 * flows on the container.
 */
public interface ContainerHost<STATE : Any, SIDE_EFFECT : Any> {
    /**
     * The orbit [Container] instance.
     *
     * Use factory functions to easily obtain a [Container] instance.
     *
     * ```
     * override val container = scope.container<MyState, MySideEffect>(initialState)
     * ```
     */
    public val container: Container<STATE, SIDE_EFFECT> // TODO temporarily var until we can figure out how to swap out for test container
}
