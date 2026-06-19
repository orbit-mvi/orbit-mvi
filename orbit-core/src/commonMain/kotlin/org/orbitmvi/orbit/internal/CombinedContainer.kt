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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.syntax.ContainerContext

/**
 * Container backing the `combine` family of [OrbitContainer] overloads.
 *
 * When [main] is `null` the combined container is purely read-only (the top-level / view-model
 * `combine` forms): its [stateFlow] is a constant [Unit] and [orbit] / [inlineOrbit] throw.
 *
 * When [main] is non-null (the receiver-form `combine` overloads) the combined container delegates
 * intents to [main], so `intent { reduce { ... } }` mutates the main's internal state while the
 * combined host still exposes the derived [externalStateFlow]. The main's internal state ([IS]) and
 * intent dispatching come from [main]; the combined external state ([R]) is derived from all
 * upstream hosts' external states.
 *
 * @param IS internal state type — the main's internal state, or [Unit] when there is no main.
 * @param R combined external state type.
 * @param SE the combined side-effect type exposed by this container.
 * @param SEM the main's own side-effect type, used only to bridge delegated intents.
 */
@Suppress("LongParameterList")
internal class CombinedContainer<IS : Any, R : Any, SE : Any, SEM : Any>(
    override val scope: CoroutineScope,
    private val main: OrbitContainer<IS, *, SEM>?,
    upstreamStateFlows: List<StateFlow<Any>>,
    upstreamSideEffectFlows: List<Flow<Any>>,
    transformState: (List<Any>) -> R,
    override val settings: RealSettings,
    private val transformSideEffects: (suspend FlowCollector<SE>.(List<Flow<Any>>) -> Unit)?,
) : OrbitContainer<IS, R, SE> {

    private val unitStateFlow: StateFlow<Unit> = MutableStateFlow(Unit).asStateFlow()
    private val combinedStateFlow: StateFlow<R> = CombinedStateFlow(upstreamStateFlows, transformState)

    @Suppress("UNCHECKED_CAST")
    override val stateFlow: StateFlow<IS> = main?.stateFlow ?: (unitStateFlow as StateFlow<IS>)

    @Suppress("UNCHECKED_CAST")
    override val refCountStateFlow: StateFlow<IS> = main?.refCountStateFlow ?: (unitStateFlow as StateFlow<IS>)

    override val externalStateFlow: StateFlow<R> = combinedStateFlow
    override val externalRefCountStateFlow: StateFlow<R> = combinedStateFlow

    /**
     * Side effects of type [SE] posted by intents running on the combined host (only relevant when a
     * [main] is present alongside a [transformSideEffects] lambda, which gives the combined host a
     * side-effect type distinct from the main's).
     */
    private val intentPosts: MutableSharedFlow<SE> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = resolveBufferSize(settings.sideEffectBufferSize),
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    /**
     * The user's [transformSideEffects] lambda applied to the upstream side-effect flows, gated on
     * combined-host subscribers via [SharingStarted.WhileSubscribed].
     */
    private val transformedSideEffects: Flow<SE> = if (transformSideEffects == null) {
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

    @Suppress("UNCHECKED_CAST")
    override val sideEffectFlow: Flow<SE> = when {
        // No transform: pass the main's own side effects straight through (SE == SEM here).
        transformSideEffects == null -> main?.sideEffectFlow as Flow<SE>? ?: emptyFlow()
        // Transform plus main: merge intent-posted side effects with the transformed upstream ones.
        main != null -> merge(intentPosts, transformedSideEffects)
        // Transform without main (top-level form): just the transformed upstream side effects.
        else -> transformedSideEffects
    }

    override val refCountSideEffectFlow: Flow<SE> = sideEffectFlow

    override fun orbit(orbitIntent: suspend ContainerContext<IS, SE>.() -> Unit): Job {
        val main = main ?: throw UnsupportedOperationException(READ_ONLY_MESSAGE)
        return main.orbit { orbitIntent(bridgeContext(this)) }
    }

    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<IS, SE>.() -> Unit) {
        val main = main ?: throw UnsupportedOperationException(READ_ONLY_MESSAGE)
        main.inlineOrbit { orbitIntent(bridgeContext(this)) }
    }

    /**
     * Adapts the main's [ContainerContext] (typed in the main's side-effect type [SEM]) to the
     * combined host's side-effect type [SE]. When there is no [transformSideEffects] lambda the two
     * types are identical and the context is reused as-is; otherwise side effects posted from the
     * intent are routed to [intentPosts] so they surface on the combined [sideEffectFlow].
     */
    @Suppress("UNCHECKED_CAST")
    private fun bridgeContext(context: ContainerContext<IS, SEM>): ContainerContext<IS, SE> =
        if (transformSideEffects == null) {
            context as ContainerContext<IS, SE>
        } else {
            ContainerContext(
                settings = context.settings,
                postSideEffect = { intentPosts.emit(it) },
                reduce = context.reduce,
                subscribedCounter = context.subscribedCounter,
                stateFlow = context.stateFlow,
            )
        }

    // Lifecycle is driven by the parent scope and the subscription count of [sideEffectFlow]; there
    // is no per-instance Job to cancel. Intents (when delegated) are owned by the main.
    override fun cancel(): Unit = Unit

    override suspend fun joinIntents() {
        main?.joinIntents()
    }

    private companion object {
        const val READ_ONLY_MESSAGE = "CombinedContainer without a main host is read-only"
        const val DEFAULT_BUFFER_SIZE = 64

        fun resolveBufferSize(size: Int): Int = if (size < 0) DEFAULT_BUFFER_SIZE else size
    }
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
