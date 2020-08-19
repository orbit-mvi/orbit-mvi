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

package com.babylon.orbit2.rxjava2

import com.babylon.orbit2.Builder
import com.babylon.orbit2.Operator
import com.babylon.orbit2.Orbit2Dsl
import com.babylon.orbit2.OrbitDslPlugins
import com.babylon.orbit2.VolatileContext
import io.reactivex.Single

internal class RxJava2Single<S : Any, E, E2 : Any>(
    val block: VolatileContext<S, E>.() -> Single<E2>
) : Operator<S, E>

/**
 * The observable transformer flat maps incoming [VolatileContext] for every event into a [Single] of
 * another type.
 *
 * The transformer executes on an `IO` dispatcher by default.
 *
 * @param block the lambda returning a new [Single] given the current state and event
 */
@Orbit2Dsl
fun <S : Any, SE : Any, E : Any, E2 : Any> Builder<S, SE, E>.transformRx2Single(
    block: VolatileContext<S, E>.() -> Single<E2>
): Builder<S, SE, E2> {
    OrbitDslPlugins.register(RxJava2DslPlugin)
    return Builder(
        stack + RxJava2Single(
            block
        )
    )
}
