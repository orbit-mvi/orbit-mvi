/*
 * Copyright 2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.syntax.simple

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformWhile
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter
import org.orbitmvi.orbit.syntax.ContainerContext
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.safeCast

@OrbitInternal
public data class SubclassStateContainerContext<S : Any, SE : Any, T : S>(
    public val settings: RealSettings,
    public val postSideEffect: suspend (SE) -> Unit,
    private val getState: () -> T,
    public val reduce: suspend ((T) -> S) -> Unit,
    public val subscribedCounter: SubscribedCounter,
) {
    public val state: T
        get() = getState()
}

@OrbitDsl
public class SubclassStateSimpleSyntax<S : Any, SE : Any, T : S>(public val containerContext: SubclassStateContainerContext<S, SE, T>) {

    /**
     * The current state which can change throughout execution of the orbit block
     */
    public val state: T get() = containerContext.state
}

/**
 * Side effects allow you to deal with things like tracking, navigation etc.
 *
 * These are delivered through [Container.sideEffectFlow] by calling [SimpleSyntax.postSideEffect].
 *
 * @param sideEffect the side effect to post through the side effect flow
 */
@OrbitDsl
public suspend fun <S : Any, SE : Any, T : S> SubclassStateSimpleSyntax<S, SE, T>.postSideEffect(sideEffect: SE) {
    containerContext.postSideEffect(sideEffect)
}

/**
 * Reducers reduce the current state and incoming events to produce a new state.
 *
 * @param reducer the lambda reducing the current state and incoming event to produce a new state
 */
@OrbitDsl
public suspend fun <S : Any, SE : Any, T : S> SubclassStateSimpleSyntax<S, SE, T>.reduce(reducer: SimpleContext<T>.() -> S) {
    containerContext.reduce { reducerState ->
        SimpleContext(reducerState).reducer()
    }
}

/**
 * This API is intended to simplify and add type-safety to working with sealed class states.
 *
 * Executes the given block only if the current state is of the given subtype and the given [predicate] matches.
 *
 * The block will be cancelled as soon as the state changes to a different type or the predicate does not return true.
 * Note that this does not guarantee the operation in the block is atomic.
 *
 * The state is captured and does not change within this block.
 *
 * @param clazz the class of the state to match. Must be a subclass of the [Container]'s state type.
 * @param predicate optional predicate to match the state against. Defaults to true.
 */
@OrbitExperimental
@OrbitDsl
public suspend fun <S : Any, SE : Any, T : S> SimpleSyntax<S, SE>.runOn(
    clazz: KClass<T>,
    predicate: (T) -> Boolean = { true },
    block: suspend SubclassStateSimpleSyntax<S, SE, T>.() -> Unit
) {
    containerContext.stateFlow
        .runOn(clazz, predicate) {
            SubclassStateSimpleSyntax(containerContext.toSubclassContainerContext(clazz, predicate, it)).block()
        }
}

/**
 * This API is intended to simplify and add type-safety to working with sealed class states.
 * This can be applied to any [Flow] of states, not just the [Container]'s own. The main purpose of this API is to help you
 * work with child container states.
 *
 * Executes the given block only if the current state is of the given subtype and the given [predicate] matches.
 *
 * The block will be cancelled as soon as the state changes to a different type or the predicate does not return true.
 * Note that this does not guarantee the operation in the block is atomic.
 *
 * The state is captured and does not change within this block.
 *
 * @param clazz the class of the state to match. Must be a subclass of the [Container]'s state type.
 * @param predicate optional predicate to match the state against. Defaults to true.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@OrbitExperimental
@OrbitDsl
public suspend fun <S : Any, T : S> Flow<S>.runOn(
    clazz: KClass<T>,
    predicate: (T) -> Boolean = { true },
    block: suspend (capturedState: T) -> Unit
) {
    this.transformWhile {
        if (clazz.isInstance(it) && predicate(clazz.cast(it))) {
            emit(clazz.cast(it))
            true
        } else {
            emit(null)
            false
        }
    }
        .distinctUntilChangedBy { it != null }
        .mapLatest {
            if (it != null) {
                block(it)
            }
        }
        .firstOrNull()
}

@OrbitInternal
private fun <S : Any, SE : Any, T : S> ContainerContext<S, SE>.toSubclassContainerContext(
    clazz: KClass<T>,
    predicate: (T) -> Boolean = { true },
    capturedState: T,
): SubclassStateContainerContext<S, SE, T> {
    return SubclassStateContainerContext(
        settings = settings,
        postSideEffect = postSideEffect,
        reduce = { reducer ->
            reduce { state ->
                clazz.safeCast(state)?.takeIf(predicate)?.let { reducer(it) } ?: state
            }
        },
        subscribedCounter = subscribedCounter,
        getState = { capturedState }
    )
}
