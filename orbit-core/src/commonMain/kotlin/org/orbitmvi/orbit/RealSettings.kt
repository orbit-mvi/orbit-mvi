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
import org.orbitmvi.orbit.internal.repeatonsubscription.DelayingSubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter

public data class RealSettings(
    public val sideEffectBufferSize: Int = Channel.BUFFERED,
    public val idlingRegistry: IdlingResource = NoopIdlingResource(),
    public val eventLoopDispatcher: CoroutineDispatcher = Dispatchers.Default,
    public val intentLaunchingDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    public val exceptionHandler: CoroutineExceptionHandler? = null,
    public val repeatOnSubscribedStopTimeout: Long = SettingsBuilder.DEFAULT_SUBSCRIBED_STOP_TIMEOUT,
    public val parentScope: CoroutineScope,
    public val scope: CoroutineScope = parentScope + eventLoopDispatcher,
    public val subscribedCounter: SubscribedCounter = DelayingSubscribedCounter(scope, repeatOnSubscribedStopTimeout)
)

public class SettingsBuilder {

    public var idlingRegistry: IdlingResource = NoopIdlingResource()

    public var exceptionHandler: CoroutineExceptionHandler? = null

    public var repeatOnSubscribedStopTimeout: Long = DEFAULT_SUBSCRIBED_STOP_TIMEOUT

    public var sideEffectBufferSize: Int = Channel.BUFFERED

    public fun apply(settings: RealSettings): RealSettings {
        return settings.copy(
            sideEffectBufferSize = sideEffectBufferSize,
            idlingRegistry = idlingRegistry,
            exceptionHandler = exceptionHandler,
            repeatOnSubscribedStopTimeout = repeatOnSubscribedStopTimeout,
        )
    }
    internal companion object {
        const val DEFAULT_SUBSCRIBED_STOP_TIMEOUT = 100L
    }
}
