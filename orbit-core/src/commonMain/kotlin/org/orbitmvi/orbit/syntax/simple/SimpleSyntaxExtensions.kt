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

package org.orbitmvi.orbit.syntax.simple

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.idling.withIdling
import org.orbitmvi.orbit.internal.runBlocking

/**
 * Reducers reduce the current state and incoming events to produce a new state.
 *
 * @param reducer the lambda reducing the current state and incoming event to produce a new state
 */
@OrbitDsl
public suspend fun <S : Any, SE : Any> SimpleSyntax<S, SE>.reduce(reducer: SimpleContext<S>.() -> S) {
    containerContext.reduce { reducerState ->
        SimpleContext(reducerState).reducer()
    }
}

/**
 * Side effects allow you to deal with things like tracking, navigation etc.
 *
 * These are delivered through [Container.sideEffectFlow] by calling [SimpleSyntax.postSideEffect].
 *
 * @param sideEffect the side effect to post through the side effect flow
 */
@OrbitDsl
public suspend fun <S : Any, SE : Any> SimpleSyntax<S, SE>.postSideEffect(sideEffect: SE) {
    containerContext.postSideEffect(sideEffect)
}

/**
 * Build and execute an intent on [Container].
 *
 * @param registerIdling whether to register an idling resource when executing this intent. Defaults to true.
 * @param transformer lambda representing the transformer
 */
@OrbitDsl
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.intent(
    registerIdling: Boolean = true,
    transformer: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit
): Unit =
    runBlocking {
        container.orbit {
            withIdling(registerIdling) {
                SimpleSyntax(this).transformer()
            }
        }
    }

/**
 * Build and execute an intent on [Container] in a blocking manner, without dispatching.
 *
 * This API is reserved for special cases e.g. storing text input in the state.
 *
 * @param registerIdling whether to register an idling resource when executing this intent. Defaults to true.
 * @param transformer lambda representing the transformer
 */
@OrbitDsl
@OrbitExperimental
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.blockingIntent(
    registerIdling: Boolean = true,
    transformer: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit
): Unit =
    runBlocking {
        container.inlineOrbit {
            withIdling(registerIdling) {
                SimpleSyntax(this).transformer()
            }
        }
    }

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@OrbitDsl
public suspend fun <S : Any, SE : Any> SimpleSyntax<S, SE>.repeatOnSubscription(
    block: suspend CoroutineScope.() -> Unit
) {
    coroutineScope {
        launch {
            containerContext.subscribedCounter.subscribed.mapLatest {
                if (it.isSubscribed) block() else null
            }.collect()
        }
    }
}
