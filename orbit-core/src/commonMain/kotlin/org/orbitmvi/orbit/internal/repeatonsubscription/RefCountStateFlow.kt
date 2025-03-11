/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class RefCountStateFlow<T>(
    private val subscribedCounter: SubscribedCounter,
    private val upStream: StateFlow<T>
) : StateFlow<T> {

    override val replayCache: List<T>
        get() = upStream.replayCache

    override val value: T
        get() = upStream.value

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        try {
            subscribedCounter.increment()
            upStream.collect(collector)
        } finally {
            subscribedCounter.decrement()
        }
    }
}

internal fun <T> StateFlow<T>.refCount(subscribedCounter: SubscribedCounter): StateFlow<T> =
    RefCountStateFlow(subscribedCounter, this)
