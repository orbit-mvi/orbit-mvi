/*
 * Copyright 2022-2023 Mikołaj Leszczyński & Appmattus Limited
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
import org.orbitmvi.orbit.idling.IdlingResource

public data class RealSettings constructor(
    public val sideEffectBufferSize: Int = OrbitSettings.sideEffectBufferSize,
    public val idlingRegistry: IdlingResource = OrbitSettings.idlingRegistry,
    public val eventLoopDispatcher: CoroutineDispatcher = OrbitSettings.eventLoopDispatcher,
    public val intentLaunchingDispatcher: CoroutineDispatcher = OrbitSettings.intentLaunchingDispatcher,
    public val exceptionHandler: CoroutineExceptionHandler? = OrbitSettings.exceptionHandler,
    public val repeatOnSubscribedStopTimeout: Long = OrbitSettings.repeatOnSubscribedStopTimeout,
    public val loggers: List<Logger> = OrbitSettings.loggers
)

public class SettingsBuilder {
    internal val settings: RealSettings
        get() = RealSettings(
            idlingRegistry = idlingRegistry,
            exceptionHandler = exceptionHandler,
            repeatOnSubscribedStopTimeout = repeatOnSubscribedStopTimeout,
            loggers = loggers
        )

    public var idlingRegistry: IdlingResource = OrbitSettings.idlingRegistry

    public var exceptionHandler: CoroutineExceptionHandler? = OrbitSettings.exceptionHandler

    public var repeatOnSubscribedStopTimeout: Long = OrbitSettings.repeatOnSubscribedStopTimeout

    public var loggers: List<Logger> = OrbitSettings.loggers
}

@Suppress("DEPRECATION")
internal fun Container.Settings.toRealSettings() = RealSettings(
    sideEffectBufferSize = sideEffectBufferSize,
    idlingRegistry = idlingRegistry,
    eventLoopDispatcher = intentDispatcher,
    intentLaunchingDispatcher = intentDispatcher,
    exceptionHandler = exceptionHandler,
    repeatOnSubscribedStopTimeout = repeatOnSubscribedStopTimeout
)
