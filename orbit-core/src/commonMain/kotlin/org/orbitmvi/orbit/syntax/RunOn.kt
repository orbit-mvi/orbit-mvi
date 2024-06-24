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
 */

package org.orbitmvi.orbit.syntax

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformWhile
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.annotation.OrbitInternal

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
 * @param predicate optional predicate to match the state against. Defaults to true.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@OrbitExperimental
@OrbitDsl
public suspend inline fun <S : Any, reified T : S> Flow<S>.runOn(
    crossinline predicate: (T) -> Boolean = { true },
    crossinline block: suspend (capturedState: T) -> Unit
) {
    this.transformWhile {
        if (it is T && predicate(it)) {
            emit(it)
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
public inline fun <S : Any, SE : Any, reified T : S> ContainerContext<S, SE>.toSubclassContainerContext(
    crossinline predicate: (T) -> Boolean = { true },
    capturedState: T,
): SubStateContainerContext<S, SE, T> {
    return SubStateContainerContext(
        settings = settings,
        postSideEffect = postSideEffect,
        reduce = { reducer ->
            reduce { state ->
                (state as? T)?.takeIf(predicate)?.let { reducer(it) } ?: state
            }
        },
        subscribedCounter = subscribedCounter,
        getState = { capturedState }
    )
}
