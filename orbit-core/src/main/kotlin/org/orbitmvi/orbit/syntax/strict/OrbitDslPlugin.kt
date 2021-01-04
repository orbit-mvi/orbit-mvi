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

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.Operator
import kotlinx.coroutines.flow.Flow

/**
 * Extend this interface to create your own DSL plugin.
 */
public interface OrbitDslPlugin {
    public fun <S : Any, E, SE : Any> apply(
        containerContext: ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        createContext: (event: E) -> VolatileContext<S, E>
    ): Flow<Any?>

    public class ContainerContext<S : Any, SE : Any>(
        public val settings: Container.Settings,
        public val postSideEffect: suspend (SE) -> Unit,
        private val getState: () -> S,
        public val reduce: suspend ((S) -> S) -> Unit
    ) {
        public val state: S
            get() = getState()
    }
}
