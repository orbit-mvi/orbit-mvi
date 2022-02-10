/*
 * Copyright 2021-2022 Mikołaj Leszczyński & Appmattus Limited
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
import org.orbitmvi.orbit.Container.Settings
import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import org.orbitmvi.orbit.internal.RealContainer
import org.orbitmvi.orbit.internal.TestContainerDecorator

/**
 * Helps create a concrete container in a standard way.
 *
 * @param initialState The initial state of the container.
 * @param settings The [Settings] to set the container up with.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner when the container is first interacted with in any way.
 * @return A [Container] implementation
 */
public fun <STATE : Any, SIDE_EFFECT : Any> CoroutineScope.container(
    initialState: STATE,
    settings: Settings = Settings(),
    onCreate: ((state: STATE) -> Unit)? = null
): Container<STATE, SIDE_EFFECT> =
    if (onCreate == null) {
        TestContainerDecorator(
            initialState,
            parentScope = this,
            RealContainer(
                initialState = initialState,
                settings = settings,
                parentScope = this
            )
        )
    } else {
        TestContainerDecorator(
            initialState,
            parentScope = this,
            LazyCreateContainerDecorator(
                RealContainer(
                    initialState = initialState,
                    settings = settings,
                    parentScope = this
                ),
                onCreate
            )
        )
    }
