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

package org.orbitmvi.orbit.syntax.strict

import org.orbitmvi.orbit.syntax.Operator
import org.orbitmvi.orbit.syntax.OrbitDsl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OrbitDsl
public class Builder<S : Any, SE : Any, E>(private val stack: List<Operator<S, *>> = emptyList()) {

    public fun <E2> add(operator: Operator<S, E2>): Builder<S, SE, E2> {
        return Builder(stack + operator)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun build(
        pluginContext: OrbitDslPlugin.ContainerContext<S, SE>
    ): Flow<Any?> {
        return stack.fold(flowOf(Unit)) { flow: Flow<Any?>, operator: Operator<S, *> ->
            orbitDslPlugins.plugins.fold(flow) { flow2: Flow<Any?>, plugin: OrbitDslPlugin ->
                plugin.apply(
                    pluginContext,
                    flow2,
                    operator as Operator<S, Any?>
                ) {
                    object : VolatileContext<S, Any?> {
                        override val state = volatileState()
                        override val event = it
                        override fun volatileState() = pluginContext.state
                    }
                }
            }
        }
    }
}
