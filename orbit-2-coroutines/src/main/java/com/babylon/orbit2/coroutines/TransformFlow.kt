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

import com.babylon.orbit2.Builder
import com.babylon.orbit2.Context
import com.babylon.orbit2.Operator
import com.babylon.orbit2.Orbit2Dsl
import com.babylon.orbit2.OrbitDslPlugins
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

internal class TransformFlow<S : Any, E, E2>(
    val registerIdling: Boolean,
    val block: suspend Context<S, E>.() -> Flow<E2>
) : Operator<S, E>

/**
 * The flow transformer flat maps incoming [Context] for every event into coroutine flows.
 *
 * The transformer executes on [Dispatchers.IO] by default.
 *
 * @param registerIdling When true registers the calls idling state, default: false
 * @param block the suspending lambda returning a new flow of events given the current state and event
 */
@Orbit2Dsl
fun <S : Any, SE : Any, E, E2> Builder<S, SE, E>.transformFlow(
    registerIdling: Boolean = false,
    block: suspend Context<S, E>.() -> Flow<E2>
): Builder<S, SE, E2> {
    OrbitDslPlugins.register(CoroutineDslPlugin)
    return Builder(stack + TransformFlow(registerIdling, block))
}
