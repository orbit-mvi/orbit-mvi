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

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.SideEffectMode
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription

internal interface SideEffectProvider<SIDE_EFFECT> {
    val sideEffectFlow: Flow<SIDE_EFFECT>

    suspend fun postSideEffect(sideEffect: SIDE_EFFECT)

    suspend fun initialise(subscribedCounter: SubscribedCounter)

    companion object {
        @OptIn(OrbitExperimental::class)
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
    // replay = 0: live effects are broadcast to currently active collectors only and never re-delivered to a
    // re-subscribing collector. Caching for the no-subscriber case is handled explicitly via [pendingCache].
    private val sharedFlow = MutableSharedFlow<SIDE_EFFECT>(
        replay = 0,
        extraBufferCapacity = resolveBufferSize(settings.sideEffectBufferSize),
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    // Holds only effects emitted while there were no subscribers. Replayed to each collector as it subscribes
    // (so multiple consumers connecting in this window all receive them) and cleared once a subscription stabilises.
    // Bounded to the configured buffer size via [cacheSlots]: once full, further effects are not cached but
    // broadcast live to the next collector (see [postSideEffect]). Slots are returned when the cache is cleared.
    private val cacheCapacity = resolveBufferSize(settings.sideEffectBufferSize).coerceAtLeast(1)
    private val cacheMutex = Mutex()
    private val cacheSlots = Semaphore(permits = cacheCapacity)
    private val pendingCache = ArrayDeque<SIDE_EFFECT>()

    override val sideEffectFlow: Flow<SIDE_EFFECT> = sharedFlow.onSubscription {
        val cached = cacheMutex.withLock { pendingCache.toList() }
        cached.forEach { emit(it) }
    }

    override suspend fun postSideEffect(sideEffect: SIDE_EFFECT) {
        val decision = cacheMutex.withLock {
            when {
                // Broadcast live to active collectors (never cached). An active collector registers (incrementing
                // subscriptionCount) before its onSubscription block runs, so an effect is either broadcast or cached.
                sharedFlow.subscriptionCount.value > 0 -> CacheDecision.LIVE
                // No subscribers: reserve a bounded cache slot for replay to the next collector.
                cacheSlots.tryAcquire() -> {
                    pendingCache.add(sideEffect)
                    CacheDecision.CACHED
                }
                // No subscribers and the cache is full: do not cache. Fall through to a live emission that
                // suspends until a collector connects, so the overflow is broadcast live rather than re-cached.
                else -> CacheDecision.OVERFLOW
            }
        }
        // Emit/await outside the lock so a suspending emit cannot block a subscribing collector's drain.
        when (decision) {
            CacheDecision.LIVE -> sharedFlow.emit(sideEffect)
            CacheDecision.CACHED -> Unit
            CacheDecision.OVERFLOW -> {
                sharedFlow.subscriptionCount.first { it > 0 }
                sharedFlow.emit(sideEffect)
            }
        }
    }

    override suspend fun initialise(subscribedCounter: SubscribedCounter) {
        var previousSubscription = Subscription.Unsubscribed
        subscribedCounter.subscribed.collect { subscription ->
            if (previousSubscription == Subscription.Unsubscribed && subscription == Subscription.Subscribed) {
                delay(settings.sideEffectReplayClearDelayMs)
                cacheMutex.withLock {
                    repeat(pendingCache.size) { cacheSlots.release() }
                    pendingCache.clear()
                }
            }
            previousSubscription = subscription
        }
    }
}

private enum class CacheDecision { LIVE, CACHED, OVERFLOW }

private const val DEFAULT_BUFFER_SIZE = 64

private fun resolveBufferSize(size: Int): Int =
    if (size < 0) DEFAULT_BUFFER_SIZE else size
