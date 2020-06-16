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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class Transform<S : Any, E : Any, E2 : Any>(val block: Context<S, E>.() -> E2) :
    Operator<S, E2>

internal class SideEffect<S : Any, SE : Any, E : Any>(val block: SideEffectContext<S, SE, E>.() -> Unit) :
    Operator<S, E>

internal class Reduce<S : Any, E : Any>(val block: Context<S, E>.() -> Any) :
    Operator<S, E>

data class SideEffectContext<S : Any, SE : Any, E : Any>(
    val state: S,
    val event: E,
    private val postSideEffect: (SE) -> Unit
) {
    fun post(event: SE) {
        postSideEffect(event)
    }
}

fun <S : Any, SE : Any, E : Any, E2 : Any> Builder<S, SE, E>.transform(block: Context<S, E>.() -> E2): Builder<S, SE, E2> {
    return Builder(
        stack + Transform(
            block
        )
    )
}

fun <S : Any, SE : Any, E : Any> Builder<S, SE, E>.sideEffect(block: SideEffectContext<S, SE, E>.() -> Unit): Builder<S, SE, E> {
    return Builder(
        stack + SideEffect(
            block
        )
    )
}

fun <S : Any, SE : Any, E : Any> Builder<S, SE, E>.reduce(block: Context<S, E>.() -> S): Builder<S, SE, E> {
    return Builder(
        stack + Reduce(
            block
        )
    )
}

object OrbitBasePlugin : OrbitPlugin {
    override fun <S : Any, E : Any, SE : Any> apply(
        containerContext: OrbitPlugin.ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        createContext: (event: E) -> Context<S, E>
    ): Flow<Any> {
        @Suppress("UNCHECKED_CAST")
        return when (operator) {
            is Transform<*, *, *> -> flow.map {
                with(operator as Transform<S, E, Any>) {
                    createContext(it).block()
                }
            }
            is SideEffect<*, *, *> -> flow.onEach {
                with(operator as SideEffect<S, SE, E>) {
                    createContext(it).let { context ->
                        SideEffectContext(
                            context.state,
                            context.event,
                            containerContext.postSideEffect
                        )
                    }
                        .block()
                }
            }
            is Reduce -> flow.onEach { event ->
                with(operator) {
                    containerContext.setState.send(
                        createContext(event).block() as S
                    )
                }
            }
            else -> flow
        }
    }
}
