/*
 * Copyright 2021 Mikolaj Leszczynski & Matthew Dolan
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

package org.orbitmvi.orbit.livedata

import androidx.lifecycle.LiveData
import org.orbitmvi.orbit.syntax.Operator
import org.orbitmvi.orbit.syntax.OrbitDsl
import org.orbitmvi.orbit.syntax.strict.Builder
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugins
import org.orbitmvi.orbit.syntax.strict.VolatileContext

internal class LiveDataOperator<S : Any, E, E2 : Any>(
    override val registerIdling: Boolean,
    val block: VolatileContext<S, E>.() -> LiveData<E2>
) : Operator<S, E2>

/**
 * The transformer flat maps incoming [VolatileContext] for every event into a [LiveData] of
 * another type.
 *
 * The transformer executes on an `IO` dispatcher by default.
 *
 * @param registerIdling When true tracks the block's idling state, default: false
 * @param block the lambda returning a new observable of events given the current state and event
 */
@OrbitDsl
fun <S : Any, SE : Any, E : Any, E2 : Any> Builder<S, SE, E>.transformLiveData(
    registerIdling: Boolean = false,
    block: VolatileContext<S, E>.() -> LiveData<E2>
): Builder<S, SE, E2> {
    OrbitDslPlugins.register(LiveDataDslPlugin)
    return add(
        LiveDataOperator(
            registerIdling,
            block
        )
    )
}
