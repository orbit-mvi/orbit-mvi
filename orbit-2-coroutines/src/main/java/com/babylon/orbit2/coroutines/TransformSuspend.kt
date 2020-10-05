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

package com.babylon.orbit2.coroutines

import com.babylon.orbit2.syntax.strict.Builder
import com.babylon.orbit2.syntax.Operator
import com.babylon.orbit2.syntax.Orbit2Dsl
import com.babylon.orbit2.syntax.strict.OrbitDslPlugins
import com.babylon.orbit2.syntax.strict.VolatileContext
import kotlinx.coroutines.Dispatchers

internal class TransformSuspend<S : Any, E, E2>(
    override val registerIdling: Boolean,
    val block: suspend VolatileContext<S, E>.() -> E2
) : Operator<S, E2>

/**
 * The suspend transformer maps the incoming state and event into a new event using a suspending
 * lambda.
 *
 * The transformer executes on [Dispatchers.IO] by default.
 *
 * @param registerIdling When true tracks the block's idling state, default: true
 * @param block the suspending lambda returning a new event given the current state and event
 */
@Orbit2Dsl
fun <S : Any, SE : Any, E, E2> Builder<S, SE, E>.transformSuspend(
    registerIdling: Boolean = true,
    block: suspend VolatileContext<S, E>.() -> E2
): Builder<S, SE, E2> {
    OrbitDslPlugins.register(CoroutineDslPlugin)
    return Builder(stack + TransformSuspend(registerIdling, block))
}
