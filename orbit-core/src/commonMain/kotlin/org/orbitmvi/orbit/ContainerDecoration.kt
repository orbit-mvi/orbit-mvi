/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.CoroutineScope
import org.orbitmvi.orbit.annotation.OrbitExperimental

/**
 * Decorates every container created through [CoroutineScope.orbitContainer], for diagnostic
 * tooling that needs to intercept container behaviour — e.g. a time-travel decorator
 * substituting the exposed state flows.
 *
 * Install via [SettingsBuilder.containerDecoration], typically once for the whole application
 * through [Orbit.configureDefaults]. The decoration is applied outermost, wrapping the fully
 * constructed container, so the flows exposed by the returned container are the ones consumers
 * observe. When no decoration is installed ([RealSettings.containerDecoration] is `null`)
 * containers are returned undecorated, exactly as before.
 */
@OrbitExperimental
public interface ContainerDecoration {

    /**
     * Returns the container to expose to callers — either [container] unchanged or a wrapper
     * around it.
     *
     * Wrappers should implement [OrbitContainerDecorator] so that tooling which unwraps
     * decorator chains (such as orbit-test) keeps working.
     */
    public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> decorate(
        container: OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>
    ): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>
}
