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

package org.orbitmvi.orbit.observer

import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.SettingsBuilder
import org.orbitmvi.orbit.annotation.OrbitExperimental

/**
 * Observes the lifecycle events of Orbit containers for diagnostic purposes — logging,
 * debugging and dev tooling.
 *
 * Install via [SettingsBuilder.eventObserver], typically once for the whole application
 * through [org.orbitmvi.orbit.Orbit.configureDefaults]. When no observer is installed
 * ([RealSettings.eventObserver] is `null`) containers skip all instrumentation and run
 * exactly as before.
 *
 * Callbacks are invoked synchronously on the thread where the event occurs — containers
 * run intents concurrently, so callbacks may arrive concurrently from multiple threads.
 * Implementations must be thread-safe and must return quickly; any expensive work should
 * be handed off to another thread.
 *
 * Callbacks must not throw. They run inside the container's own machinery, so a throwing
 * observer can break the container it observes — up to and including stopping its intent
 * dispatch loop. Observers own their error handling; the container deliberately does not
 * pay for defensive wrapping on the unobserved hot path's behalf.
 *
 * The state and side effect objects passed to [onReduction] and [onSideEffect] are the
 * container's live objects, not copies. Observers must treat them as immutable, in line
 * with Orbit's existing immutability contract for states.
 */
@OrbitExperimental
public interface OrbitEventObserver {

    /**
     * A container was created.
     *
     * @param containerInfo the container's identity, display name and initial state.
     */
    public fun onContainerCreated(containerInfo: ContainerInfo)

    /**
     * A container's scope completed and it will emit no further events.
     *
     * Reported when the [kotlinx.coroutines.Job] of the container's scope completes. A container
     * whose parent scope has no job (e.g. [kotlinx.coroutines.GlobalScope]) is never reported
     * as closed.
     *
     * @param containerId the id previously reported via [onContainerCreated].
     */
    public fun onContainerClosed(containerId: Long)

    /**
     * An intent was taken off the container's dispatch queue and is about to be launched.
     *
     * @param containerId the id of the container executing the intent.
     * @param intentId identifies this intent execution; unique within the container.
     * @param name a best-effort display name for the intent, or `null` when unavailable.
     */
    public fun onIntentDispatched(containerId: Long, intentId: Long, name: String?)

    /**
     * An intent previously reported via [onIntentDispatched] finished.
     *
     * @param containerId the id of the container that executed the intent.
     * @param intentId the id previously reported via [onIntentDispatched].
     * @param result whether the intent completed, was cancelled, or failed. An intent whose
     * exception was consumed by an installed
     * [org.orbitmvi.orbit.SettingsBuilder.exceptionHandler] is still reported as
     * [IntentResult.Failed].
     */
    public fun onIntentCompleted(containerId: Long, intentId: Long, result: IntentResult)

    /**
     * A reduction was applied to the container's state.
     *
     * Reports the winning transition only: when concurrent reductions force the underlying
     * compare-and-set loop to retry a reducer, discarded attempts are not reported and
     * [oldState] is the state the winning attempt was actually applied to.
     *
     * Reported for every reduction, including those where [oldState] equals [newState]
     * (which the container's state flow deduplicates and never emits).
     *
     * Callbacks for reductions racing on different threads may arrive out of application
     * order; each report's [oldState]→[newState] pair is exact, so consumers needing order
     * should link transitions by content rather than by arrival.
     *
     * @param containerId the id of the container that reduced.
     * @param intentId the id of the intent this reduction is attributed to, or `null` when
     * the reduction did not originate from a tracked intent.
     * @param oldState the state the reducer was applied to.
     * @param newState the state the reducer produced.
     */
    public fun onReduction(containerId: Long, intentId: Long?, oldState: Any, newState: Any)

    /**
     * A side effect was posted, before it is delivered to the side effect channel.
     *
     * @param containerId the id of the container that posted the side effect.
     * @param intentId the id of the intent this side effect is attributed to, or `null` when
     * the side effect did not originate from a tracked intent.
     * @param sideEffect the posted side effect.
     */
    public fun onSideEffect(containerId: Long, intentId: Long?, sideEffect: Any)
}

/**
 * Identity of a container as reported to [OrbitEventObserver.onContainerCreated].
 *
 * @property containerId uniquely identifies the container for the lifetime of the process.
 * @property containerName the display name configured via [SettingsBuilder.containerName],
 * or `null` when not configured. Names are display-only and need not be unique.
 * @property initialState the state the container was created with.
 */
@OrbitExperimental
public class ContainerInfo(
    public val containerId: Long,
    public val containerName: String?,
    public val initialState: Any,
)

/**
 * The terminal result of an intent execution, reported to
 * [OrbitEventObserver.onIntentCompleted].
 */
@OrbitExperimental
public sealed class IntentResult {

    /** The intent ran to completion. */
    public data object Completed : IntentResult()

    /** The intent was cancelled before it could complete. */
    public data object Cancelled : IntentResult()

    /**
     * The intent threw [exception]. Reported regardless of whether an installed
     * [org.orbitmvi.orbit.SettingsBuilder.exceptionHandler] consumed the exception.
     */
    public class Failed(public val exception: Throwable) : IntentResult()
}
