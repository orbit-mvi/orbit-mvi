/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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
import org.orbitmvi.orbit.logger.Logger

/**
 * Global Settings for Orbit Multiplatform
 */
public object OrbitSettings {
    public var sideEffectBufferSize: Int = Channel.BUFFERED

    public var idlingRegistry: IdlingResource = NoopIdlingResource()

    public var eventLoopDispatcher: CoroutineDispatcher = Dispatchers.Default

    public var intentLaunchingDispatcher: CoroutineDispatcher = Dispatchers.Unconfined

    public var exceptionHandler: CoroutineExceptionHandler? = null

    @Suppress("MagicNumber")
    public var repeatOnSubscribedStopTimeout: Long = 100L

    public var loggers: List<Logger> = emptyList()
}
