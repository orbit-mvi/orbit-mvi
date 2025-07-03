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

package org.orbitmvi.orbit

import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.internal.RealContainerWithExternalState

/**
 * The heart of the Orbit MVI system. Represents an MVI container with its input and outputs.
 * You can manipulate the container through the [orbit] function
 *
 * @param INTERNAL_STATE The container's internal state type.
 * @param EXTERNAL_STATE The container's external state type.
 * @param SIDE_EFFECT The type of side effects posted by this container. Can be [Nothing] if this
 * container never posts side effects.
 */
public interface ContainerWithExternalState<INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> :
    ContainerDecorator<INTERNAL_STATE, SIDE_EFFECT> {

    public val mapToExternalState: (internalState: INTERNAL_STATE) -> EXTERNAL_STATE

    public val externalStateFlow: StateFlow<EXTERNAL_STATE>

    public val externalRefCountStateFlow: StateFlow<EXTERNAL_STATE>
}

public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> Container<INTERNAL_STATE, SIDE_EFFECT>.mapToExternalState(
    transform: (INTERNAL_STATE) -> EXTERNAL_STATE
): ContainerWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
    return RealContainerWithExternalState(
        actual = this,
        mapToExternalState = transform
    )
}
