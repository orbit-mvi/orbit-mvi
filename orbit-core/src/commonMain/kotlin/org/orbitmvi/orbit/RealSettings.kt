/*
 * Copyright 2022-2026 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.idling.IdlingResource
import org.orbitmvi.orbit.idling.NoopIdlingResource
import org.orbitmvi.orbit.observer.OrbitEventObserver

/**
 * Controls how side effects are delivered to collectors.
 */
public enum class SideEffectMode {
    /**
     * Each side effect is delivered to exactly one collector (fan-out).
     * Cached side effects are consumed by the first collector that connects.
     * If multiple collectors are active at the same time they will silently compete for events.
     */
    FAN_OUT,

    /**
     * Each side effect is delivered to exactly one collector (fan-out).
     * Cached side effects are consumed by the first collector that connects.
     * Prevents multiple collectors from silently competing for side effect events:
     * a second collector receives an [IllegalStateException].
     */
    FAN_OUT_STRICT,

    /**
     * Side effects are broadcast to all active collectors.
     * Cached side effects are replayed to all collectors when they reconnect.
     * The replay cache is cleared shortly after subscribers reconnect to prevent stale replay.
     */
    @OrbitExperimental
    BROADCAST
}

public data class RealSettings(
    public val sideEffectBufferSize: Int = Channel.BUFFERED,
    public val idlingRegistry: IdlingResource = NoopIdlingResource(),
    public val eventLoopDispatcher: () -> CoroutineDispatcher = { Dispatchers.Default },
    public val intentLaunchingDispatcher: () -> CoroutineDispatcher = { Dispatchers.Unconfined },
    public val exceptionHandler: CoroutineExceptionHandler? = null,
    public val repeatOnSubscribedStopTimeout: Long = 100L,
    public val sideEffectMode: SideEffectMode = SideEffectMode.FAN_OUT,
    public val sideEffectReplayClearDelayMs: Long = 100L,
    @OrbitExperimental
    public val containerName: String? = null,
    @OrbitExperimental
    public val eventObserver: OrbitEventObserver? = null,
    @OrbitExperimental
    public val containerDecoration: ContainerDecoration? = null,
)

public class SettingsBuilder internal constructor(baseline: RealSettings) {
    public constructor() : this(Orbit.defaultSettings)

    internal var settings = baseline
        private set

    public var idlingRegistry: IdlingResource
        get() = settings.idlingRegistry
        public set(value) {
            settings = settings.copy(idlingRegistry = value)
        }

    /**
     * Factory for the dispatcher the container's event loop runs on. Invoked once per container,
     * so each container gets its own dispatcher instance — e.g. its own
     * [kotlinx.coroutines.CoroutineDispatcher.limitedParallelism] view.
     */
    public var eventLoopDispatcher: () -> CoroutineDispatcher
        get() = settings.eventLoopDispatcher
        public set(value) {
            settings = settings.copy(eventLoopDispatcher = value)
        }

    /**
     * Factory for the dispatcher intents are launched on. Invoked once per container,
     * so each container gets its own dispatcher instance — e.g. its own
     * [kotlinx.coroutines.CoroutineDispatcher.limitedParallelism] view, giving per-container
     * (not app-wide) parallelism limits.
     */
    public var intentLaunchingDispatcher: () -> CoroutineDispatcher
        get() = settings.intentLaunchingDispatcher
        public set(value) {
            settings = settings.copy(intentLaunchingDispatcher = value)
        }

    public var exceptionHandler: CoroutineExceptionHandler?
        get() = settings.exceptionHandler
        public set(value) {
            settings = settings.copy(exceptionHandler = value)
        }

    public var repeatOnSubscribedStopTimeout: Long
        get() = settings.repeatOnSubscribedStopTimeout
        public set(value) {
            settings = settings.copy(repeatOnSubscribedStopTimeout = value)
        }

    public var sideEffectBufferSize: Int
        get() = settings.sideEffectBufferSize
        public set(value) {
            settings = settings.copy(sideEffectBufferSize = value)
        }

    public var sideEffectMode: SideEffectMode
        get() = settings.sideEffectMode
        public set(value) {
            settings = settings.copy(sideEffectMode = value)
        }

    public var sideEffectReplayClearDelayMs: Long
        get() = settings.sideEffectReplayClearDelayMs
        public set(value) {
            settings = settings.copy(sideEffectReplayClearDelayMs = value)
        }

    /**
     * A human-readable display name for the container, reported to the installed
     * [org.orbitmvi.orbit.observer.OrbitEventObserver]. Names are display-only and need not
     * be unique. Defaults to `null`.
     */
    @OrbitExperimental
    public var containerName: String?
        get() = settings.containerName
        public set(value) {
            settings = settings.copy(containerName = value)
        }

    /**
     * Observes container lifecycle events for diagnostic purposes. When `null` (the default)
     * containers skip all instrumentation.
     *
     * See [org.orbitmvi.orbit.observer.OrbitEventObserver] for the threading contract.
     */
    @OrbitExperimental
    public var eventObserver: OrbitEventObserver?
        get() = settings.eventObserver
        public set(value) {
            settings = settings.copy(eventObserver = value)
        }

    /**
     * Decorates every container created through [kotlinx.coroutines.CoroutineScope.orbitContainer].
     * When `null` (the default) containers are returned undecorated.
     *
     * See [ContainerDecoration].
     */
    @OrbitExperimental
    public var containerDecoration: ContainerDecoration?
        get() = settings.containerDecoration
        public set(value) {
            settings = settings.copy(containerDecoration = value)
        }
}
