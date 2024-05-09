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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.plus
import org.orbitmvi.orbit.idling.IdlingResource
import org.orbitmvi.orbit.idling.NoopIdlingResource

public data class RealSettings(
    public val sideEffectBufferSize: Int = Channel.BUFFERED,
    public val idlingRegistry: IdlingResource = NoopIdlingResource(),
    public val eventLoopDispatcher: CoroutineDispatcher = Dispatchers.Default,
    public val intentLaunchingDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    public val exceptionHandler: CoroutineExceptionHandler? = null,
    public val repeatOnSubscribedStopTimeout: Long = 100L,
    public val parentScope: CoroutineScope
) {
    public val containerScope: CoroutineScope = parentScope + eventLoopDispatcher
}

public class SettingsBuilder(parentScope: CoroutineScope) {
    internal var settings = RealSettings(parentScope = parentScope)
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
}
