/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.compiler.State
import org.orbitmvi.orbit.compiler.TestContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce

// Class used in ContainerHostPluginTest
@Suppress("unused")
class StandardContainerHost : ContainerHost<State, Int>, TestContainerHost {
    override val container = CoroutineScope(Dispatchers.Unconfined).container<State, Int>(State.Loading)

    override fun triggerReadyState(parameter: Int) = intent {
        reduce {
            State.Ready(
                triggerLoadingState = { intent { reduce { State.Loading } } },
                triggerSideEffect = { intent { postSideEffect(parameter) } }
            )
        }
    }
}
