/*
 * Copyright 2022-2024 Mikołaj Leszczyński & Appmattus Limited
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
import org.orbitmvi.orbit.idling.IdlingResource
import org.orbitmvi.orbit.idling.NoopIdlingResource

/**
 * Controls how side effects are delivered to collectors.
 */
public enum class SideEffectMode {
    /**
     * Each side effect is delivered to exactly one collector (fan-out).
     * Cached side effects are consumed by the first collector that connects.
     */
    FAN_OUT,

    /**
     * Side effects are broadcast to all active collectors.
     * Cached side effects are replayed to all collectors when they reconnect.
     * The replay cache is cleared shortly after subscribers reconnect to prevent stale replay.
     */
    BROADCAST
}

public data class RealSettings(
    public val sideEffectBufferSize: Int = Channel.BUFFERED,
    public val idlingRegistry: IdlingResource = NoopIdlingResource(),
    public val eventLoopDispatcher: CoroutineDispatcher = Dispatchers.Default,
    public val intentLaunchingDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    public val exceptionHandler: CoroutineExceptionHandler? = null,
    public val repeatOnSubscribedStopTimeout: Long = 100L,
    public val sideEffectMode: SideEffectMode = SideEffectMode.BROADCAST,
    public val sideEffectReplayClearDelayMs: Long = 100L,
)

public class SettingsBuilder {
    internal var settings = RealSettings()
        private set

    public var idlingRegistry: IdlingResource
        get() = settings.idlingRegistry
        public set(value) {
            settings = settings.copy(idlingRegistry = value)
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
}
