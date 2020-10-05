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

import com.babylon.orbit2.internal.LazyCreateContainerDecorator
import com.babylon.orbit2.internal.RealContainer
import kotlinx.coroutines.CoroutineScope

/**
 * Helps create a concrete container in a standard way.
 *
 * @param initialState The initial state of the container.
 * @param settings The [Settings] to set the container up with.
 * @param onCreate The lambda to execute when the container is created. By default it is
 * executed in a lazy manner when the container is first interacted with in any way.
 * @return A [Container] implementation
 */
fun <STATE : Any, SIDE_EFFECT : Any> CoroutineScope.container(
    initialState: STATE,
    settings: Container.Settings = Container.Settings(),
    onCreate: ((state: STATE) -> Unit)? = null
): Container<STATE, SIDE_EFFECT> =
    if (onCreate == null) {
        RealContainer(
            initialState = initialState,
            settings = settings,
            parentScope = this
        )
    } else {
        LazyCreateContainerDecorator(
            RealContainer(
                initialState = initialState,
                settings = settings,
                parentScope = this
            ),
            onCreate
        )
    }
