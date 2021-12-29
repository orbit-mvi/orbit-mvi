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

package org.orbitmvi.orbit.idling

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.orbitmvi.orbit.syntax.ContainerContext
import org.orbitmvi.orbit.syntax.Operator

public suspend fun <O : Operator<*, *>, T> ContainerContext<*, *>.withIdling(
    operator: O,
    block: suspend O.() -> T
): T {
    if (operator.registerIdling) settings.idlingRegistry.increment()
    return block(operator).also {
        if (operator.registerIdling) settings.idlingRegistry.decrement()
    }
}

public suspend fun <O : Operator<*, *>, T> ContainerContext<*, *>.withIdlingFlow(
    operator: O,
    block: suspend O.() -> Flow<T>
): Flow<T> {
    return block(operator)
        .onStart { if (operator.registerIdling) settings.idlingRegistry.increment() }
        .onCompletion { if (operator.registerIdling) settings.idlingRegistry.decrement() }
}
