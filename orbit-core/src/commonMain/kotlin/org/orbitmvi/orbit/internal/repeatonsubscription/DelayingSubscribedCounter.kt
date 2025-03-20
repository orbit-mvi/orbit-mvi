/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Subscribed
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Unsubscribed
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.incrementAndFetch

internal class DelayingSubscribedCounter(
    scope: CoroutineScope,
    private val repeatOnSubscribedStopTimeout: Long
) : SubscribedCounter {

    private val _subscribed: Channel<Subscription> = Channel(Channel.BUFFERED)

    @OptIn(FlowPreview::class)
    override val subscribed: Flow<Subscription> = _subscribed
        .receiveAsFlow()
        .debounce { if (it == Unsubscribed) repeatOnSubscribedStopTimeout else 0 }
        .stateIn(scope, started = SharingStarted.Eagerly, initialValue = Unsubscribed)

    private val counter = AtomicInt(0)

    override suspend fun increment() {
        counter.incrementAndFetch()
        _subscribed.send(Subscribed)
    }

    override suspend fun decrement() {
        if (counter.updateAndGet { if (it > 0) it - 1 else 0 } == 0) {
            _subscribed.send(Unsubscribed)
        }
    }
}

private inline fun AtomicInt.updateAndGet(function: (Int) -> Int): Int {
    while (true) {
        val cur = load()
        val upd = function(cur)
        if (compareAndSet(cur, upd)) return upd
    }
}
