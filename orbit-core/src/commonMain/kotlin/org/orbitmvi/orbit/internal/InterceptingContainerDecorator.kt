/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.internal

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.syntax.ContainerContext

public class InterceptingContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    override val actual: RealContainer<STATE, SIDE_EFFECT>
) : ContainerDecorator<STATE, SIDE_EFFECT> {
    public val savedIntents: Channel<suspend () -> Unit> = Channel(Channel.UNLIMITED)

    override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit): Job {
        savedIntents.send { actual.pluginContext.orbitIntent() }
        return Job()
    }

    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        savedIntents.send { actual.pluginContext.orbitIntent() }
    }
}
