/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator

internal class SavedStateContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    override val actual: Container<STATE, SIDE_EFFECT>,
    private val savedStateHandle: SavedStateHandle,
    private val serializer: KSerializer<STATE>,
    private val savedStateHandleKey: String
) : ContainerDecorator<STATE, SIDE_EFFECT> {

    override val stateFlow: StateFlow<STATE> by lazy {
        actual.stateFlow.onEach {
            savedStateHandle[savedStateHandleKey] = encodeToSavedState(
                serializer = serializer,
                value = it
            )
        }
    }

    override val refCountStateFlow: StateFlow<STATE> by lazy {
        actual.refCountStateFlow.onEach {
            savedStateHandle[savedStateHandleKey] = encodeToSavedState(
                serializer = serializer,
                value = it
            )
        }
    }
}
