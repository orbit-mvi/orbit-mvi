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

import kotlinx.coroutines.runBlocking
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.idling.withIdling
import org.orbitmvi.orbit.syntax.Syntax

/**
 * Build and execute an intent on [Container] in a blocking manner, without dispatching.
 *
 * This API is reserved for special cases e.g. storing text input in the state.
 *
 * @param registerIdling whether to register an idling resource when executing this intent. Defaults to true.
 * @param transformer lambda representing the transformer
 */
@OrbitDsl
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.blockingIntent(
    registerIdling: Boolean = true,
    transformer: suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit
): Unit = runBlocking {
    container.inlineOrbit {
        withIdling(registerIdling) {
            Syntax(this).transformer()
        }
    }
}

/**
 * Build and execute an intent on [ContainerWithExternalState] in a blocking manner, without dispatching.
 *
 * This API is reserved for special cases e.g. storing text input in the state.
 *
 * @param registerIdling whether to register an idling resource when executing this intent. Defaults to true.
 * @param transformer lambda representing the transformer
 */
@OrbitDsl
public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any>
    ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>.blockingIntent(
        registerIdling: Boolean = true,
        transformer: suspend Syntax<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit
    ): Unit = runBlocking {
    container.inlineOrbit {
        withIdling(registerIdling) {
            Syntax(this).transformer()
        }
    }
}
