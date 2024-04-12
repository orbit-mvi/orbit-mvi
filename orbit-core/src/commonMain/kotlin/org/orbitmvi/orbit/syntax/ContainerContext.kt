/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.syntax

import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter

@OrbitInternal
public data class ContainerContext<S : Any, SE : Any>(
    public val settings: RealSettings,
    public val postSideEffect: suspend (SE) -> Unit,
    public val reduce: suspend ((S) -> S) -> Unit,
    public val subscribedCounter: SubscribedCounter,
    public val stateFlow: StateFlow<S>,
) {
    public val state: S
        get() = stateFlow.value
}
