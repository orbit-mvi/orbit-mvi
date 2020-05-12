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
    override val state: S,
    override val event: E,
    private val postSideEffect: (SE) -> Unit
) : Context<S, E> {
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

object BasePlugin : OrbitPlugin {
    override fun <S : Any, E : Any, SE : Any> apply(
        containerContext: OrbitPlugin.ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        context: (event: E) -> Context<S, E>
    ): Flow<Any> {
        return when (operator) {
            is Transform<*, *, *> -> flow.map {
                @Suppress("UNCHECKED_CAST")
                with(operator as Transform<S, E, Any>) {
                    context(it).block()
                }
            }
            is SideEffect<*, *, *> -> flow.onEach {
                with(operator as SideEffect<S, SE, E>) {
                    val baseContext = context(it)
                    SideEffectContext(
                        baseContext.state,
                        baseContext.event,
                        containerContext.postSideEffect
                    ).block()
                }
            }
            is Reduce -> flow.onEach {
                with(operator) {
                    containerContext.setState { context(it).block() as S }
                }
            }
            else -> flow
        }
    }
}

fun requirePlugin(plugin: OrbitPlugin, operatorName: String) {
    require(Orbit.plugins.contains(plugin)) {
        throw IllegalStateException(
            "${plugin.javaClass.simpleName} required to use $operatorName! " +
                    "Install plugins using Orbit.registerPlugins."
        )
    }
}
