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

package org.orbitmvi.orbit.viewmodel

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin

internal class SavedStateContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    override val actual: Container<STATE, SIDE_EFFECT>,
    private val savedStateHandle: SavedStateHandle
) : ContainerDecorator<STATE, SIDE_EFFECT> {
    override val currentState: STATE
        get() = actual.currentState

    override val stateFlow: Flow<STATE>
        get() = flow {
            actual.stateFlow.collect {
                savedStateHandle[SAVED_STATE_KEY] = it
                emit(it)
            }
        }
    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = actual.sideEffectFlow

    override fun orbit(orbitFlow: suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) =
        actual.orbit(orbitFlow)
}
