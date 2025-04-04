/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.syntax

import kotlinx.coroutines.Job
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.idling.withIdling

/**
 * Build and execute an intent on [Container].
 *
 * @param registerIdling whether to register an idling resource when executing this intent. Defaults to true.
 * @param transformer lambda representing the transformer
 */
@OrbitDsl
internal fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.intent(
    registerIdling: Boolean = true,
    transformer: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit
): Job = orbit {
    withIdling(registerIdling) {
        transformer()
    }
}
