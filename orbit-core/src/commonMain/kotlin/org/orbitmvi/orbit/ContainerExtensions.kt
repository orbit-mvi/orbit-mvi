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

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

context(containerHost: ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>)
public val <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> Container<INTERNAL_STATE, SIDE_EFFECT>.externalStateFlow: StateFlow<EXTERNAL_STATE>
    get() = stateFlow.stateMap(containerHost::mapToExternalState)

context(containerHost: ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>)
public val <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> Container<INTERNAL_STATE, SIDE_EFFECT>.externalRefCountStateFlow: StateFlow<EXTERNAL_STATE>
    get() = refCountStateFlow.stateMap(containerHost::mapToExternalState)

/**
 * A state flow that maps values using the [transform] function.
 * Note: Neither the value being mapped or the mapped value can be null.
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class MappedStateFlow<T : Any, U : Any>(
    private val upstream: StateFlow<T>,
    private val transform: (T) -> U
) : StateFlow<U> {

    override val replayCache: List<U>
        get() = upstream.replayCache.map(transform)

    override val value: U
        get() = transform(upstream.value)

    override suspend fun collect(collector: FlowCollector<U>): Nothing {
        var previous: Any? = null
        upstream.collect {
            val value = transform(it)
            if (previous == null || value != previous) {
                previous = value
                collector.emit(value)
            }
        }
    }
}

private fun <T : Any, U : Any> StateFlow<T>.stateMap(transform: (T) -> U): StateFlow<U> =
    MappedStateFlow(this, transform)
