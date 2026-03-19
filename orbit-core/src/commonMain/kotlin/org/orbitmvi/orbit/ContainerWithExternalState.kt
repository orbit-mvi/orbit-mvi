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

@file:Suppress("DEPRECATION")

package org.orbitmvi.orbit

import org.orbitmvi.orbit.internal.ExternalStateContainerAdapter

/**
 * Wraps a [Container] with an external state transformation.
 *
 * @param transformState The function that transforms the internal state to the external state.
 * @return An [OrbitContainer] with the external state transformation applied.
 */
@Deprecated(
    "Use the scope.container(initialState, transformState) factory function instead",
    ReplaceWith("scope.container(initialState, transformState)")
)
public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> Container<INTERNAL_STATE, SIDE_EFFECT>.withExternalState(
    transformState: (INTERNAL_STATE) -> EXTERNAL_STATE
): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
    return ExternalStateContainerAdapter(
        delegate = this,
        externalTransformState = transformState
    )
}
