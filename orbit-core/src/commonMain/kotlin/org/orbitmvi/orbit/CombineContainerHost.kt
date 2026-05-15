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

@file:Suppress("UNCHECKED_CAST", "TooManyFunctions", "LongParameterList", "MagicNumber")

package org.orbitmvi.orbit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.internal.CombinedContainer

// -------- Receiver-extension forms — use the receiver's container.scope --------

/**
 * Combines the receiver and [other] into a read-only [OrbitContainerHost] whose external state is
 * derived from both upstream hosts' external states.
 *
 * Side effects from upstream hosts are not forwarded. The returned host's side-effect type is
 * [Nothing] and its side-effect flows complete immediately on subscription.
 *
 * The receiver's `container.scope` is used to host the combined container.
 */
@OrbitExperimental
public fun <IS1 : Any, ES1 : Any, ES2 : Any, R : Any>
    OrbitContainerHost<IS1, ES1, *>.combine(
        other: OrbitContainerHost<*, ES2, *>,
        transformState: (ES1, ES2) -> R,
    ): OrbitContainerHost<Unit, R, Nothing> =
    combine(container.scope, this, other, transformState)

@OrbitExperimental
public fun <IS1 : Any, ES1 : Any, ES2 : Any, ES3 : Any, R : Any>
    OrbitContainerHost<IS1, ES1, *>.combine(
        host2: OrbitContainerHost<*, ES2, *>,
        host3: OrbitContainerHost<*, ES3, *>,
        transformState: (ES1, ES2, ES3) -> R,
    ): OrbitContainerHost<Unit, R, Nothing> =
    combine(container.scope, this, host2, host3, transformState)

@OrbitExperimental
public fun <IS1 : Any, ES1 : Any, ES2 : Any, ES3 : Any, ES4 : Any, R : Any>
    OrbitContainerHost<IS1, ES1, *>.combine(
        host2: OrbitContainerHost<*, ES2, *>,
        host3: OrbitContainerHost<*, ES3, *>,
        host4: OrbitContainerHost<*, ES4, *>,
        transformState: (ES1, ES2, ES3, ES4) -> R,
    ): OrbitContainerHost<Unit, R, Nothing> =
    combine(container.scope, this, host2, host3, host4, transformState)

@OrbitExperimental
public fun <IS1 : Any, ES1 : Any, ES2 : Any, ES3 : Any, ES4 : Any, ES5 : Any, R : Any>
    OrbitContainerHost<IS1, ES1, *>.combine(
        host2: OrbitContainerHost<*, ES2, *>,
        host3: OrbitContainerHost<*, ES3, *>,
        host4: OrbitContainerHost<*, ES4, *>,
        host5: OrbitContainerHost<*, ES5, *>,
        transformState: (ES1, ES2, ES3, ES4, ES5) -> R,
    ): OrbitContainerHost<Unit, R, Nothing> =
    combine(container.scope, this, host2, host3, host4, host5, transformState)

/**
 * Combines the receiver and [other] into a read-only [OrbitContainerHost]. State is derived via
 * [transformState]; side effects are merged via [transformSideEffects], which is invoked while the
 * combined host has at least one subscriber to [OrbitContainer.sideEffectFlow] and emits values of
 * type [T] using the [FlowCollector] receiver.
 *
 * Use [emitAll] with [kotlinx.coroutines.flow.merge] (or any custom transform) inside
 * [transformSideEffects] to forward upstream side effects. The lambda must not return early; once it
 * does, no further side effects are emitted for the current subscription window. The typical pattern
 * is:
 *
 * ```
 * transformSideEffects = { se1, se2 -> emitAll(merge(se1, se2)) }
 * ```
 *
 * The combined side-effect flow is broadcast to all current collectors with no replay cache. Upstream
 * subscription is gated on combined-host subscribers, so when no one is collecting the combined
 * side-effect flow the lambda is suspended and upstream `repeatOnSubscription` blocks observe the
 * unsubscribed state.
 *
 * Caution: in [SideEffectMode.FAN_OUT] mode the combined host *consumes* upstream side effects while
 * subscribed, which means other direct collectors of those flows will not receive them. Prefer
 * [SideEffectMode.BROADCAST] when an upstream host participates in a combine.
 */
@OrbitExperimental
public fun <IS1 : Any, ES1 : Any, SE1 : Any, ES2 : Any, SE2 : Any, R : Any, T : Any>
    OrbitContainerHost<IS1, ES1, SE1>.combine(
        other: OrbitContainerHost<*, ES2, SE2>,
        transformState: (ES1, ES2) -> R,
        transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>) -> Unit,
    ): OrbitContainerHost<Unit, R, T> =
    combine(container.scope, this, other, transformState, transformSideEffects)

@OrbitExperimental
public fun <IS1 : Any, ES1 : Any, SE1 : Any, ES2 : Any, SE2 : Any, ES3 : Any, SE3 : Any, R : Any, T : Any>
    OrbitContainerHost<IS1, ES1, SE1>.combine(
        host2: OrbitContainerHost<*, ES2, SE2>,
        host3: OrbitContainerHost<*, ES3, SE3>,
        transformState: (ES1, ES2, ES3) -> R,
        transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>, Flow<SE3>) -> Unit,
    ): OrbitContainerHost<Unit, R, T> =
    combine(container.scope, this, host2, host3, transformState, transformSideEffects)

@OrbitExperimental
public fun <
    IS1 : Any,
    ES1 : Any,
    SE1 : Any,
    ES2 : Any,
    SE2 : Any,
    ES3 : Any,
    SE3 : Any,
    ES4 : Any,
    SE4 : Any,
    R : Any,
    T : Any,
    > OrbitContainerHost<IS1, ES1, SE1>.combine(
    host2: OrbitContainerHost<*, ES2, SE2>,
    host3: OrbitContainerHost<*, ES3, SE3>,
    host4: OrbitContainerHost<*, ES4, SE4>,
    transformState: (ES1, ES2, ES3, ES4) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>, Flow<SE3>, Flow<SE4>) -> Unit,
): OrbitContainerHost<Unit, R, T> =
    combine(container.scope, this, host2, host3, host4, transformState, transformSideEffects)

@OrbitExperimental
public fun <
    IS1 : Any,
    ES1 : Any,
    SE1 : Any,
    ES2 : Any,
    SE2 : Any,
    ES3 : Any,
    SE3 : Any,
    ES4 : Any,
    SE4 : Any,
    ES5 : Any,
    SE5 : Any,
    R : Any,
    T : Any,
    > OrbitContainerHost<IS1, ES1, SE1>.combine(
    host2: OrbitContainerHost<*, ES2, SE2>,
    host3: OrbitContainerHost<*, ES3, SE3>,
    host4: OrbitContainerHost<*, ES4, SE4>,
    host5: OrbitContainerHost<*, ES5, SE5>,
    transformState: (ES1, ES2, ES3, ES4, ES5) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>, Flow<SE3>, Flow<SE4>, Flow<SE5>) -> Unit,
): OrbitContainerHost<Unit, R, T> =
    combine(container.scope, this, host2, host3, host4, host5, transformState, transformSideEffects)

// -------- Top-level forms — caller provides the scope for the combined container --------

@OrbitExperimental
public fun <ES1 : Any, ES2 : Any, R : Any> combine(
    scope: CoroutineScope,
    host1: OrbitContainerHost<*, ES1, *>,
    host2: OrbitContainerHost<*, ES2, *>,
    transformState: (ES1, ES2) -> R,
): OrbitContainerHost<Unit, R, Nothing> = createCombinedHost(
    scope = scope,
    hosts = listOf(host1, host2),
    transformState = { values -> transformState(values[0] as ES1, values[1] as ES2) },
    transformSideEffects = null,
)

@OrbitExperimental
public fun <ES1 : Any, ES2 : Any, ES3 : Any, R : Any> combine(
    scope: CoroutineScope,
    host1: OrbitContainerHost<*, ES1, *>,
    host2: OrbitContainerHost<*, ES2, *>,
    host3: OrbitContainerHost<*, ES3, *>,
    transformState: (ES1, ES2, ES3) -> R,
): OrbitContainerHost<Unit, R, Nothing> = createCombinedHost(
    scope = scope,
    hosts = listOf(host1, host2, host3),
    transformState = { values -> transformState(values[0] as ES1, values[1] as ES2, values[2] as ES3) },
    transformSideEffects = null,
)

@OrbitExperimental
public fun <ES1 : Any, ES2 : Any, ES3 : Any, ES4 : Any, R : Any> combine(
    scope: CoroutineScope,
    host1: OrbitContainerHost<*, ES1, *>,
    host2: OrbitContainerHost<*, ES2, *>,
    host3: OrbitContainerHost<*, ES3, *>,
    host4: OrbitContainerHost<*, ES4, *>,
    transformState: (ES1, ES2, ES3, ES4) -> R,
): OrbitContainerHost<Unit, R, Nothing> = createCombinedHost(
    scope = scope,
    hosts = listOf(host1, host2, host3, host4),
    transformState = { values ->
        transformState(values[0] as ES1, values[1] as ES2, values[2] as ES3, values[3] as ES4)
    },
    transformSideEffects = null,
)

@OrbitExperimental
public fun <ES1 : Any, ES2 : Any, ES3 : Any, ES4 : Any, ES5 : Any, R : Any> combine(
    scope: CoroutineScope,
    host1: OrbitContainerHost<*, ES1, *>,
    host2: OrbitContainerHost<*, ES2, *>,
    host3: OrbitContainerHost<*, ES3, *>,
    host4: OrbitContainerHost<*, ES4, *>,
    host5: OrbitContainerHost<*, ES5, *>,
    transformState: (ES1, ES2, ES3, ES4, ES5) -> R,
): OrbitContainerHost<Unit, R, Nothing> = createCombinedHost(
    scope = scope,
    hosts = listOf(host1, host2, host3, host4, host5),
    transformState = { values ->
        transformState(values[0] as ES1, values[1] as ES2, values[2] as ES3, values[3] as ES4, values[4] as ES5)
    },
    transformSideEffects = null,
)

@OrbitExperimental
public fun <ES1 : Any, SE1 : Any, ES2 : Any, SE2 : Any, R : Any, T : Any> combine(
    scope: CoroutineScope,
    host1: OrbitContainerHost<*, ES1, SE1>,
    host2: OrbitContainerHost<*, ES2, SE2>,
    transformState: (ES1, ES2) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>) -> Unit,
): OrbitContainerHost<Unit, R, T> = createCombinedHost(
    scope = scope,
    hosts = listOf(host1, host2),
    transformState = { values -> transformState(values[0] as ES1, values[1] as ES2) },
    transformSideEffects = { flows ->
        transformSideEffects(flows[0] as Flow<SE1>, flows[1] as Flow<SE2>)
    },
)

@OrbitExperimental
public fun <ES1 : Any, SE1 : Any, ES2 : Any, SE2 : Any, ES3 : Any, SE3 : Any, R : Any, T : Any> combine(
    scope: CoroutineScope,
    host1: OrbitContainerHost<*, ES1, SE1>,
    host2: OrbitContainerHost<*, ES2, SE2>,
    host3: OrbitContainerHost<*, ES3, SE3>,
    transformState: (ES1, ES2, ES3) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>, Flow<SE3>) -> Unit,
): OrbitContainerHost<Unit, R, T> = createCombinedHost(
    scope = scope,
    hosts = listOf(host1, host2, host3),
    transformState = { values -> transformState(values[0] as ES1, values[1] as ES2, values[2] as ES3) },
    transformSideEffects = { flows ->
        transformSideEffects(flows[0] as Flow<SE1>, flows[1] as Flow<SE2>, flows[2] as Flow<SE3>)
    },
)

@OrbitExperimental
public fun <ES1 : Any, SE1 : Any, ES2 : Any, SE2 : Any, ES3 : Any, SE3 : Any, ES4 : Any, SE4 : Any, R : Any, T : Any> combine(
    scope: CoroutineScope,
    host1: OrbitContainerHost<*, ES1, SE1>,
    host2: OrbitContainerHost<*, ES2, SE2>,
    host3: OrbitContainerHost<*, ES3, SE3>,
    host4: OrbitContainerHost<*, ES4, SE4>,
    transformState: (ES1, ES2, ES3, ES4) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>, Flow<SE3>, Flow<SE4>) -> Unit,
): OrbitContainerHost<Unit, R, T> = createCombinedHost(
    scope = scope,
    hosts = listOf(host1, host2, host3, host4),
    transformState = { values ->
        transformState(values[0] as ES1, values[1] as ES2, values[2] as ES3, values[3] as ES4)
    },
    transformSideEffects = { flows ->
        transformSideEffects(flows[0] as Flow<SE1>, flows[1] as Flow<SE2>, flows[2] as Flow<SE3>, flows[3] as Flow<SE4>)
    },
)

@OrbitExperimental
public fun <
    ES1 : Any,
    SE1 : Any,
    ES2 : Any,
    SE2 : Any,
    ES3 : Any,
    SE3 : Any,
    ES4 : Any,
    SE4 : Any,
    ES5 : Any,
    SE5 : Any,
    R : Any,
    T : Any,
    > combine(
    scope: CoroutineScope,
    host1: OrbitContainerHost<*, ES1, SE1>,
    host2: OrbitContainerHost<*, ES2, SE2>,
    host3: OrbitContainerHost<*, ES3, SE3>,
    host4: OrbitContainerHost<*, ES4, SE4>,
    host5: OrbitContainerHost<*, ES5, SE5>,
    transformState: (ES1, ES2, ES3, ES4, ES5) -> R,
    transformSideEffects: suspend FlowCollector<T>.(Flow<SE1>, Flow<SE2>, Flow<SE3>, Flow<SE4>, Flow<SE5>) -> Unit,
): OrbitContainerHost<Unit, R, T> = createCombinedHost(
    scope = scope,
    hosts = listOf(host1, host2, host3, host4, host5),
    transformState = { values ->
        transformState(values[0] as ES1, values[1] as ES2, values[2] as ES3, values[3] as ES4, values[4] as ES5)
    },
    transformSideEffects = { flows ->
        transformSideEffects(
            flows[0] as Flow<SE1>,
            flows[1] as Flow<SE2>,
            flows[2] as Flow<SE3>,
            flows[3] as Flow<SE4>,
            flows[4] as Flow<SE5>,
        )
    },
)

private fun <R : Any, SE : Any> createCombinedHost(
    scope: CoroutineScope,
    hosts: List<OrbitContainerHost<*, *, *>>,
    transformState: (List<Any>) -> R,
    transformSideEffects: (suspend FlowCollector<SE>.(List<Flow<Any>>) -> Unit)?,
): OrbitContainerHost<Unit, R, SE> {
    val combinedContainer = CombinedContainer<R, SE>(
        scope = scope,
        upstreamStateFlows = hosts.map { it.container.externalRefCountStateFlow as StateFlow<Any> },
        upstreamSideEffectFlows = hosts.map { it.container.refCountSideEffectFlow as Flow<Any> },
        transformState = transformState,
        settings = hosts.first().container.settings,
        transformSideEffects = transformSideEffects,
    )
    return object : OrbitContainerHost<Unit, R, SE> {
        override val container: OrbitContainer<Unit, R, SE> = combinedContainer
    }
}
