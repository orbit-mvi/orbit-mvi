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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.SideEffectMode
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription
import kotlin.concurrent.atomics.AtomicBoolean

internal interface SideEffectProvider<SIDE_EFFECT> {
    val sideEffectFlow: Flow<SIDE_EFFECT>

    suspend fun postSideEffect(sideEffect: SIDE_EFFECT)

    suspend fun initialise(subscribedCounter: SubscribedCounter)

    companion object {
        fun <SIDE_EFFECT> create(settings: RealSettings): SideEffectProvider<SIDE_EFFECT> =
            when (settings.sideEffectMode) {
                SideEffectMode.FAN_OUT -> FanOutSideEffectProvider(settings)
                SideEffectMode.FAN_OUT_STRICT -> StrictFanOutSideEffectProvider(settings)
                SideEffectMode.BROADCAST -> BroadcastSideEffectProvider(settings)
            }
    }
}

private class FanOutSideEffectProvider<SIDE_EFFECT>(
    settings: RealSettings,
) : SideEffectProvider<SIDE_EFFECT> {
    private val channel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)

    override val sideEffectFlow: Flow<SIDE_EFFECT> = channel.receiveAsFlow()

    override suspend fun postSideEffect(sideEffect: SIDE_EFFECT) {
        channel.send(sideEffect)
    }

    override suspend fun initialise(subscribedCounter: SubscribedCounter) = Unit
}

private class StrictFanOutSideEffectProvider<SIDE_EFFECT>(
    settings: RealSettings,
) : SideEffectProvider<SIDE_EFFECT> {
    private val channel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)

    override val sideEffectFlow: Flow<SIDE_EFFECT> = channel.consumeAsFlow()

    override suspend fun postSideEffect(sideEffect: SIDE_EFFECT) {
        channel.send(sideEffect)
    }

    override suspend fun initialise(subscribedCounter: SubscribedCounter) = Unit
}

private class BroadcastSideEffectProvider<SIDE_EFFECT>(
    private val settings: RealSettings,
) : SideEffectProvider<SIDE_EFFECT> {
    private val sharedFlow = MutableSharedFlow<SIDE_EFFECT>(
        replay = resolveBufferSize(settings.sideEffectBufferSize),
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    override val sideEffectFlow: Flow<SIDE_EFFECT> = sharedFlow

    override suspend fun postSideEffect(sideEffect: SIDE_EFFECT) {
        sharedFlow.emit(sideEffect)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun initialise(subscribedCounter: SubscribedCounter) {
        var previousSubscription = Subscription.Unsubscribed
        subscribedCounter.subscribed.collect { subscription ->
            if (previousSubscription == Subscription.Unsubscribed && subscription == Subscription.Subscribed) {
                delay(settings.sideEffectReplayClearDelayMs)
                sharedFlow.resetReplayCache()
            }
            previousSubscription = subscription
        }
    }
}

private const val DEFAULT_BUFFER_SIZE = 64

private fun resolveBufferSize(size: Int): Int =
    if (size < 0) DEFAULT_BUFFER_SIZE else size
