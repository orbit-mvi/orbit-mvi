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
import org.orbitmvi.orbit.compiler.TestInnerContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce

// Class used in ContainerHostPluginTest
@Suppress("unused")
class InnerClassContainerHost : TestInnerContainerHost {

    // Usually private ContainerHost to not expose the caller to the internal implementation
    // Made public for testing
    override val host = InnerHost()

    override fun triggerReadyState(parameter: Int) = host.intent {
        reduce {
            State.Ready(
                triggerLoadingState = { host.intent { reduce { State.Loading } } },
                triggerSideEffect = { host.intent { postSideEffect(parameter) } }
            )
        }
    }

    class InnerHost : ContainerHost<State, Int> {
        override val container = CoroutineScope(Dispatchers.Unconfined).container<State, Int>(State.Loading)
    }
}
