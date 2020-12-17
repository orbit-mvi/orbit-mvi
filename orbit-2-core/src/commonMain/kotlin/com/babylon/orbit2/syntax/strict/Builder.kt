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

package com.babylon.orbit2.syntax.strict

import com.babylon.orbit2.syntax.Operator
import com.babylon.orbit2.syntax.Orbit2Dsl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Orbit2Dsl
public class Builder<S : Any, SE : Any, E>(private val stack: List<Operator<S, *>> = emptyList()) {

    public fun <E2> add(operator: Operator<S, E2>): Builder<S, SE, E2> {
        return Builder(stack + operator)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun build(
        pluginContext: OrbitDslPlugin.ContainerContext<S, SE>
    ): Flow<Any?> {
        return stack.fold(flowOf(Unit)) { flow: Flow<Any?>, operator: Operator<S, *> ->
            OrbitDslPlugins.plugins.fold(flow) { flow2: Flow<Any?>, plugin: OrbitDslPlugin ->
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
