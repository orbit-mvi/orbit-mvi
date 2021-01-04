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

package org.orbitmvi.orbit.livedata

import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import org.orbitmvi.orbit.idling.withIdlingFlow
import org.orbitmvi.orbit.syntax.Operator
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin
import org.orbitmvi.orbit.syntax.strict.VolatileContext

/**
 * Orbit plugin providing LiveData DSL operators:
 *
 * * [transformLiveData]
 */
object LiveDataDslPlugin : OrbitDslPlugin {

    @Suppress("UNCHECKED_CAST", "EXPERIMENTAL_API_USAGE")
    override fun <S : Any, E, SE : Any> apply(
        containerContext: OrbitDslPlugin.ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        createContext: (event: E) -> VolatileContext<S, E>
    ): Flow<Any?> {
        return when (operator) {
            is LiveDataOperator<*, *, *> -> flow.flatMapConcat {
                containerContext.withIdlingFlow(operator as LiveDataOperator<S, E, Any>) {
                    createContext(it).block().asFlow().flowOn(containerContext.settings.backgroundDispatcher)
                }
            }
            else -> flow
        }
    }
}
