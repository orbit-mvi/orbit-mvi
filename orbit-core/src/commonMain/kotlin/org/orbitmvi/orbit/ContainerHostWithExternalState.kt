/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.syntax.intent

/**
 * Apply this interface to anything you want to become an orbit container host.
 * Typically this will be an Android ViewModel but it can be applied to simple presenters etc.
 *
 * Extension functions `intent` and `orbit` are provided as a convenient way of launching orbit
 * intents on the container.
 */
public interface ContainerHostWithExternalState<INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> {
    /**
     * The orbit [Container] instance.
     *
     * Use factory functions to easily obtain a [Container] instance.
     *
     * ```
     * override val container = scope.container<MyState, MySideEffect>(initialState)
     * ```
     */
    public val container: Container<INTERNAL_STATE, SIDE_EFFECT>

    public fun mapToExternalState(internalState: INTERNAL_STATE): EXTERNAL_STATE

    /**
     * Build and execute an intent on [Container].
     *
     * @param registerIdling whether to register an idling resource when executing this intent. Defaults to true.
     * @param transformer lambda representing the transformer
     */
    @OrbitDsl
    public fun intent(
        registerIdling: Boolean = true,
        transformer: suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit
    ): Job = container.intent(registerIdling) { Syntax(this).transformer() }

    /**
     * Used for parallel decomposition or subdivision of a larger intent into smaller parts.
     *
     * Should only be used from within an [intent] or [subIntent] block.
     *
     * An example use case for sub-intents is to [launch] multiple from a single intent using [coroutineScope].
     * For example, when listening to multiple flows from the [Container] `onCreate` lambda.
     *
     * ```
     * override val container = scope.container<TestState, String>(initialState) {
     *             coroutineScope {
     *                 launch {
     *                     sendSideEffect1()
     *                 }
     *                 launch {
     *                     sendSideEffect2()
     *                 }
     *             }
     *         }
     *
     * @OptIn(OrbitExperimental::class)
     * private suspend fun sendSideEffect1() = subIntent {
     *     flow1.collect {
     *         postSideEffect(it)
     *     }
     * }
     *
     * @OptIn(OrbitExperimental::class)
     * private suspend fun sendSideEffect1() = subIntent {
     *     flow2.collect {
     *         postSideEffect(it)
     *     }
     * }
     *
     * ```
     *
     * @param transformer lambda representing the transformer
     */
    @OrbitDsl
    @OrbitExperimental
    public suspend fun subIntent(
        transformer: suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit,
    ): Unit = container.inlineOrbit {
        Syntax(this).transformer()
    }
}
