/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.syntax

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.SettingsBuilder
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.annotation.OrbitExperimental

@OrbitDsl
public class Syntax<S : Any, SE : Any>(public val containerContext: ContainerContext<S, SE>) {

    /**
     * The current state which can change throughout execution of the orbit block
     */
    public val state: S get() = containerContext.state

    /**
     * Reducers reduce the current state and incoming events to produce a new state.
     *
     * @param reducer the lambda reducing the current state and incoming event to produce a new state
     */
    @OrbitDsl
    public suspend fun reduce(reducer: IntentContext<S>.() -> S) {
        containerContext.reduce { reducerState ->
            IntentContext(reducerState).reducer()
        }
    }

    /**
     * Side effects allow you to deal with things like tracking, navigation etc.
     *
     * These are delivered through [Container.sideEffectFlow] by calling [Syntax.postSideEffect].
     *
     * @param sideEffect the side effect to post through the side effect flow
     */
    @OrbitDsl
    public suspend fun postSideEffect(sideEffect: SE) {
        containerContext.postSideEffect(sideEffect)
    }

    /**
     * Starts and stops the provided block of code based on the number of subscribers to the
     * [Container.refCountStateFlow] and [Container.refCountSideEffectFlow].
     *
     * If the number of subscribers reaches non-zero, the block is run. When the number of subscribers reaches
     * zero, the block is cancelled after a short delay. The delay can be set when creating the [Container]
     * using [SettingsBuilder.repeatOnSubscribedStopTimeout].
     *
     * This API is useful for collecting hot flows.
     *
     * @param block the lambda to run when we have active subscribers.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @OrbitDsl
    public suspend fun repeatOnSubscription(
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
     * @param predicate optional predicate to match the state against. Defaults to true.
     */
    @OrbitExperimental
    @OrbitDsl
    public suspend inline fun <reified T : S> runOn(
        crossinline predicate: (T) -> Boolean = { true },
        crossinline block: suspend SubStateSyntax<S, SE, T>.() -> Unit
    ) {
        containerContext.stateFlow
            .runOn<S, T>(predicate) {
                SubStateSyntax(containerContext.toSubclassContainerContext<_, _, T>(predicate, it)).block()
            }
    }
}
