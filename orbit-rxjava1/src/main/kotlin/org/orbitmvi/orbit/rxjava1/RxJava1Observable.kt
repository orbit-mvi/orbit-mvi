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

package org.orbitmvi.orbit.rxjava1

import org.orbitmvi.orbit.syntax.strict.Builder
import org.orbitmvi.orbit.syntax.Operator
import org.orbitmvi.orbit.syntax.OrbitDsl
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugins
import org.orbitmvi.orbit.syntax.strict.VolatileContext
import rx.Observable

internal class RxJava1Observable<S : Any, E, E2>(
    override val registerIdling: Boolean,
    val block: VolatileContext<S, E>.() -> Observable<E2>
) : Operator<S, E2>

/**
 * The observable transformer flat maps incoming [VolatileContext] for every event into an [Observable] of
 * another type.
 *
 * The transformer executes on an `IO` dispatcher by default.
 *
 * @param registerIdling When true tracks the block's idling state, default: false
 * @param block the lambda returning a new observable of events given the current state and event
 */
@OrbitDsl
public fun <S : Any, SE : Any, E, E2> Builder<S, SE, E>.transformRx1Observable(
    registerIdling: Boolean = false,
    block: VolatileContext<S, E>.() -> Observable<E2>
): Builder<S, SE, E2> {
    OrbitDslPlugins.register(RxJava1DslPlugin)
    return add(RxJava1Observable(registerIdling, block))
}
