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

package org.orbitmvi.orbit.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.shareIn
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.syntax.ContainerContext

internal class CombinedContainer<R : Any, SE : Any>(
    override val scope: CoroutineScope,
    upstreamStateFlows: List<StateFlow<Any>>,
    upstreamSideEffectFlows: List<Flow<Any>>,
    transformState: (List<Any>) -> R,
    override val settings: RealSettings,
    transformSideEffects: (suspend FlowCollector<SE>.(List<Flow<Any>>) -> Unit)?,
) : OrbitContainer<Unit, R, SE> {

    private val unitStateFlow: StateFlow<Unit> = MutableStateFlow(Unit).asStateFlow()
    private val combinedStateFlow: StateFlow<R> = CombinedStateFlow(upstreamStateFlows, transformState)

    override val stateFlow: StateFlow<Unit> = unitStateFlow
    override val refCountStateFlow: StateFlow<Unit> = unitStateFlow

    override val externalStateFlow: StateFlow<R> = combinedStateFlow
    override val externalRefCountStateFlow: StateFlow<R> = combinedStateFlow

    override val sideEffectFlow: Flow<SE> = if (transformSideEffects == null) {
        emptyFlow()
    } else {
        channelFlow {
            val collector = FlowCollector<SE> { send(it) }
            with(collector) { transformSideEffects(upstreamSideEffectFlows) }
        }.shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = settings.repeatOnSubscribedStopTimeout,
                replayExpirationMillis = 0,
            ),
            replay = 0,
        )
    }

    override val refCountSideEffectFlow: Flow<SE> = sideEffectFlow

    override fun orbit(orbitIntent: suspend ContainerContext<Unit, SE>.() -> Unit): Job =
        throw UnsupportedOperationException("CombinedContainer is read-only")

    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<Unit, SE>.() -> Unit): Unit =
        throw UnsupportedOperationException("CombinedContainer is read-only")

    // Lifecycle is driven by the parent scope and the subscription count of [sideEffectFlow]; there
    // is no per-instance Job to cancel.
    override fun cancel(): Unit = Unit

    override suspend fun joinIntents(): Unit = Unit
}

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
internal class CombinedStateFlow<R : Any>(
    private val upstreams: List<StateFlow<Any>>,
    private val transform: (List<Any>) -> R,
) : StateFlow<R> {

    override val replayCache: List<R>
        get() = listOf(value)

    override val value: R
        get() = transform(upstreams.map { it.value })

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        var previous: Any? = null
        combine(upstreams) { values -> transform(values.toList()) }
            .collect { value ->
                if (previous == null || value != previous) {
                    previous = value
                    collector.emit(value)
                }
            }
        awaitCancellation()
    }
}
