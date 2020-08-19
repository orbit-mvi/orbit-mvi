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

package com.babylon.orbit2

import com.babylon.orbit2.idling.withIdling
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

internal class Transform<S : Any, E, E2>(override val registerIdling: Boolean, val block: VolatileContext<S, E>.() -> E2) :
    Operator<S, E2>

internal class SideEffect<S : Any, SE : Any, E>(override val registerIdling: Boolean, val block: SideEffectContext<S, SE, E>.() -> Unit) :
    Operator<S, E>

internal class Reduce<S : Any, E>(override val registerIdling: Boolean, val block: Context<S, E>.() -> Any) :
    Operator<S, E>

/**
 * The basic transformer maps the incoming state and event into a new event.
 *
 * The transformer executes on an `IO` dispatcher by default.
 *
 * @param registerIdling When true tracks the block's idling state, default: true
 * @param block the lambda returning a new event given the current state and event
 */
@Orbit2Dsl
fun <S : Any, SE : Any, E, E2> Builder<S, SE, E>.transform(
    registerIdling: Boolean = true,
    block: VolatileContext<S, E>.() -> E2
): Builder<S, SE, E2> {
    return Builder(stack + Transform(registerIdling, block))
}

/**
 * Side effects allow you to deal with things like tracking, navigation etc.
 *
 * There is also a special type of side effects - ones that are meant for the view to listen
 * to as one-off events that are awkward to represent as part of the state - typically things
 * like navigation, showing transient views like toasts etc.
 *
 * These are delivered through [Container.sideEffectStream] by calling [SideEffectContext.post].
 *
 * Side effects are passthrough operators. This means that after applying
 * a side effect, the upstream event flows unmodified downstream.
 *
 * @param registerIdling When true tracks the block's idling state, default: true
 * @param block the lambda executing side effects given the current state and event
 */
@Orbit2Dsl
fun <S : Any, SE : Any, E> Builder<S, SE, E>.sideEffect(
    registerIdling: Boolean = true,
    block: SideEffectContext<S, SE, E>.() -> Unit
): Builder<S, SE, E> {
    return Builder(stack + SideEffect(registerIdling, block))
}

/**
 * Reducers reduce the current state and incoming events to produce a new state.
 *
 * Reducers are passthrough operators. This means that after applying
 * a reducer, the upstream event flows unmodified downstream.
 *
 * @param registerIdling When true tracks the block's idling state, default: true
 * @param block the lambda reducing the current state and incoming event to produce a new state
 */
@Orbit2Dsl
fun <S : Any, SE : Any, E> Builder<S, SE, E>.reduce(
    registerIdling: Boolean = true,
    block: Context<S, E>.() -> S
): Builder<S, SE, E> {
    return Builder(stack + Reduce(registerIdling, block))
}

/**
 * Orbit plugin providing the basic DSL operators:
 *
 * * [transform]
 * * [sideEffect]
 * * [reduce]
 */
@Suppress("ComplexMethod")
object BaseDslPlugin : OrbitDslPlugin {
    override fun <S : Any, E, SE : Any> apply(
        containerContext: OrbitDslPlugin.ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        createContext: (event: E) -> VolatileContext<S, E>
    ): Flow<Any?> {
        @Suppress("UNCHECKED_CAST")
        return when (operator) {
            is Transform<*, *, *> -> flow.map {
                containerContext.withIdling(operator as Transform<S, E, Any>) {
                    withContext(containerContext.backgroundDispatcher) {
                        createContext(it).block()
                    }
                }
            }
            is SideEffect<*, *, *> -> flow.onEach {
                containerContext.withIdling(operator as SideEffect<S, SE, E>) {
                    createContext(it).let { context ->
                        object : SideEffectContext<S, SE, E> {
                            override val state = context.state
                            override val event = context.event
                            override fun post(event: SE) = containerContext.postSideEffect(event)
                        }
                    }.block()
                }
            }
            is Reduce -> flow.onEach { event ->
                containerContext.withIdling(operator) {
                    containerContext.setState.send(
                        createContext(event).block() as S
                    )
                }
            }
            else -> flow
        }
    }
}
