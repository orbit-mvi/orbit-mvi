/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

private class RefCountFlow<T>(
    private val subscribedCounter: SubscribedCounter,
    private val upStream: Flow<T>
) : Flow<T> {

    override suspend fun collect(collector: FlowCollector<T>) {
        try {
            subscribedCounter.increment()
            upStream.collect(collector)
        } finally {
            subscribedCounter.decrement()
        }
    }
}

internal fun <T> Flow<T>.refCount(subscribedCounter: SubscribedCounter): Flow<T> =
    RefCountFlow(subscribedCounter, this)
