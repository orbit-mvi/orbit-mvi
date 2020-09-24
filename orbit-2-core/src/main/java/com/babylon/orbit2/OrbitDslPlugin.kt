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

package com.babylon.orbit2

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

/**
 * Extend this interface to create your own DSL plugin.
 */
interface OrbitDslPlugin {
    fun <S : Any, E, SE : Any> apply(
        containerContext: ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        createContext: (event: E) -> VolatileContext<S, E>
    ): Flow<Any?>

    class ContainerContext<S : Any, SE : Any>(
        val backgroundDispatcher: CoroutineDispatcher,
        val setState: (S) -> Unit,
        val postSideEffect: (SE) -> Unit,
        val settings: Container.Settings
    )
}
