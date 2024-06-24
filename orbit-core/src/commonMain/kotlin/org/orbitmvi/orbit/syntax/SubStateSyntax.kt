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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.SettingsBuilder
import org.orbitmvi.orbit.annotation.OrbitDsl

@OrbitDsl
public class SubStateSyntax<S : Any, SE : Any, T : S>(private val containerContext: SubStateContainerContext<S, SE, T>) {

    /**
     * The current state which can change throughout execution of the orbit block
     */
    public val state: T get() = containerContext.state

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
     * Reducers reduce the current state and incoming events to produce a new state.
     *
     * @param reducer the lambda reducing the current state and incoming event to produce a new state
     */
    @OrbitDsl
    public suspend fun reduce(reducer: IntentContext<T>.() -> S) {
        containerContext.reduce { reducerState ->
            IntentContext(reducerState).reducer()
        }
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
    @OptIn(ExperimentalCoroutinesApi::class)
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
}
