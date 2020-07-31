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

package com.babylon.orbit2.rxjava3

import com.babylon.orbit2.Builder
import com.babylon.orbit2.Context
import com.babylon.orbit2.Operator
import com.babylon.orbit2.Orbit2Dsl
import com.babylon.orbit2.OrbitDslPlugins
import io.reactivex.rxjava3.core.Maybe

internal class RxJava3Maybe<S : Any, E : Any, E2 : Any>(
    val block: suspend Context<S, E>.() -> Maybe<E2>
) : Operator<S, E>

/**
 * The maybe transformer flat maps incoming [Context] for every event into a [Maybe] of
 * another type.
 *
 * The transformer executes on an `IO` dispatcher by default.
 *
 * @param block the lambda returning a new [Maybe] given the current state and event
 */
@Orbit2Dsl
fun <S : Any, SE : Any, E : Any, E2 : Any> Builder<S, SE, E>.transformRx3Maybe(
    block: suspend Context<S, E>.() -> Maybe<E2>
): Builder<S, SE, E2> {
    OrbitDslPlugins.register(RxJava3DslPlugin)
    return Builder(
        stack + RxJava3Maybe(
            block
        )
    )
}
