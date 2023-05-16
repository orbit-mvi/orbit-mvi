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

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.ContainerContext

/**
 * A decorator applying additional logic to a [Container].
 *
 * @param STATE The container's state type.
 * @param SIDE_EFFECT The type of side effects posted by this container. Can be [Nothing] if this
 * container never posts side effects.
 */
public interface ContainerDecorator<STATE : Any, SIDE_EFFECT : Any> : Container<STATE, SIDE_EFFECT> {
    /**
     * The wrapped container.
     */
    public val actual: Container<STATE, SIDE_EFFECT>

    override val settings: RealSettings
        get() = actual.settings
    override val stateFlow: StateFlow<STATE>
        get() = actual.stateFlow
    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = actual.sideEffectFlow

    override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit): Job {
        return actual.orbit(orbitIntent)
    }

    @OptIn(OrbitExperimental::class)
    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        actual.inlineOrbit(orbitIntent)
    }

    @OptIn(OrbitExperimental::class)
    override suspend fun joinIntents() {
        actual.joinIntents()
    }

    @OptIn(OrbitExperimental::class)
    override fun cancel() {
        actual.cancel()
    }
}
