/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.launch
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
internal fun <T> BroadcastChannel<T>.asStateStream(initial: () -> T): Stream<T> {
    return object : Stream<T> {
        override fun observe(lambda: (T) -> Unit): Closeable {
            val sub = this@asStateStream.openSubscription()

            CoroutineScope(Dispatchers.Unconfined).launch {
                var lastState = initial()
                lambda(lastState)

                for (state in sub) {
                    if (state != lastState) {
                        lastState = state
                        lambda(state)
                    }
                }
            }
            return Closeable { sub.cancel() }
        }
    }
}

@ExperimentalCoroutinesApi
internal fun <T> Channel<T>.asNonCachingStream(): Stream<T> {
    val broadcastChannel = this.broadcast(start = CoroutineStart.DEFAULT)

    return object : Stream<T> {
        override fun observe(lambda: (T) -> Unit): Closeable {
            val receiveChannel = broadcastChannel.openSubscription()
            CoroutineScope(Dispatchers.Unconfined).launch {
                for (item in receiveChannel) {
                    lambda(item)
                }
            }
            return Closeable {
                receiveChannel.cancel()
            }
        }
    }
}

@ExperimentalCoroutinesApi
internal fun <T> Channel<T>.asCachingStream(originalScope: CoroutineScope): Stream<T> {
    return object : Stream<T> {
        private val subCount = AtomicInteger(0)
        private val buffer = mutableListOf<T>()
        private val channel = BroadcastChannel<T>(Channel.BUFFERED)

        init {
            originalScope.launch {
                for (item in this@asCachingStream) {
                    if (subCount.get() == 0) {
                        buffer.add(item)
                    } else {
                        channel.send(item)
                    }
                }
            }
        }

        override fun observe(lambda: (T) -> Unit): Closeable {
            val receiveChannel = channel.openSubscription()

            CoroutineScope(Dispatchers.Unconfined).launch {
                if (subCount.compareAndSet(0, 1)) {
                    buffer.forEach { buffered ->
                        channel.send(buffered)
                    }
                    buffer.clear()
                }
                for (item in receiveChannel) {
                    lambda(item)
                }
            }
            return Closeable {
                receiveChannel.cancel()
                subCount.decrementAndGet()
            }
        }
    }
}
