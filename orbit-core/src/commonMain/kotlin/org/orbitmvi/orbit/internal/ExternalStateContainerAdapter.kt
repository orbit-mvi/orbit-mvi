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

package org.orbitmvi.orbit.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.syntax.ContainerContext

/**
 * Adapts an [OrbitContainer] (where internal state = external state) to an [OrbitContainer]
 * with a different external state type. Used for backwards compatibility with the deprecated
 * [org.orbitmvi.orbit.withExternalState] extension function.
 */
@OrbitInternal
public class ExternalStateContainerAdapter<INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any>(
    public val delegate: OrbitContainer<INTERNAL_STATE, INTERNAL_STATE, SIDE_EFFECT>,
    private val externalTransformState: (INTERNAL_STATE) -> EXTERNAL_STATE
) : OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {

    override val scope: CoroutineScope get() = delegate.scope
    override val settings: RealSettings get() = delegate.settings

    override val stateFlow: StateFlow<INTERNAL_STATE> get() = delegate.stateFlow
    override val refCountStateFlow: StateFlow<INTERNAL_STATE> get() = delegate.refCountStateFlow

    override val externalStateFlow: StateFlow<EXTERNAL_STATE> by lazy {
        delegate.stateFlow.stateMap(externalTransformState)
    }
    override val externalRefCountStateFlow: StateFlow<EXTERNAL_STATE> by lazy {
        delegate.refCountStateFlow.stateMap(externalTransformState)
    }

    override val sideEffectFlow: Flow<SIDE_EFFECT> get() = delegate.sideEffectFlow
    override val refCountSideEffectFlow: Flow<SIDE_EFFECT> get() = delegate.refCountSideEffectFlow

    override fun orbit(orbitIntent: suspend ContainerContext<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit): Job {
        return delegate.orbit(orbitIntent)
    }

    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit) {
        delegate.inlineOrbit(orbitIntent)
    }

    override fun cancel() {
        delegate.cancel()
    }

    override suspend fun joinIntents() {
        delegate.joinIntents()
    }
}
