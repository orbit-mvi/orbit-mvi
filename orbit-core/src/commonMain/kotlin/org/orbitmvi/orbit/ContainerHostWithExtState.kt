/*
 * Copyright 2024 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.orbitmvi.orbit.internal.repeatonsubscription.refCount

public class ContainerWithExtState<State : Any, SideEffect : Any, UiState : Any>(
    override val actual: Container<State, SideEffect>,
    mapper: (State) -> UiState,
) : ContainerDecorator<State, SideEffect> {

    public val extStateFlow: StateFlow<UiState> = stateFlow.map(mapper)
        .stateIn(scope, started = SharingStarted.WhileSubscribed(), initialValue = mapper(actual.stateFlow.value))
        .refCount(actual.settings.subscribedCounter)
}

public interface ContainerHostWithExtState<State : Any, SideEffect : Any, ExtState : Any> :
    ContainerHost<State, SideEffect> {
    override val container: ContainerWithExtState<State, SideEffect, ExtState>

    public fun Container<State, SideEffect>.withExtState(
        mapper: (State) -> ExtState,
    ): ContainerWithExtState<State, SideEffect, ExtState> = ContainerWithExtState(this, mapper)
}
