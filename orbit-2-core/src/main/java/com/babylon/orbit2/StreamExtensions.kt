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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal fun <T> Flow<T>.asStream(): Stream<T> {
    return object : Stream<T> {
        override fun observe(lambda: (T) -> Unit): Stream.Closeable {
            val scope = CoroutineScope(Dispatchers.Unconfined)
            scope.launch {
                this@asStream.collect {
                    lambda(it)
                }
            }
            return object : Stream.Closeable {
                override fun close() {
                    scope.cancel()
                }
            }
        }
    }
}

internal fun <T> Channel<T>.asStream(originalScope: CoroutineScope): Stream<T> {
    return object : Stream<T> {
        private val broadcastChannel = originalScope.broadcast(
            capacity = 1024,
            start = CoroutineStart.DEFAULT
        ) {
            for (item in this@asStream) {
                if (isActive) {
                    send(item)
                } else {
                    break
                }
            }
        }

        override fun observe(lambda: (T) -> Unit): Stream.Closeable {
            val scope = CoroutineScope(Dispatchers.Unconfined)
            val receiveChannel = broadcastChannel.openSubscription()
            scope.launch {
                for (item in receiveChannel) {
                    lambda(item)
                }
            }
            return object : Stream.Closeable {
                override fun close() {
                    receiveChannel.cancel()
                    scope.cancel()
                }
            }
        }
    }
}

internal fun <T> Channel<T>.asCachingStream(originalScope: CoroutineScope): Stream<T> {
    return object : Stream<T> {
        private val channels = mutableSetOf<ReceiveChannel<T>>()
        private val buffer = mutableListOf<T>()
        private val bufferMutex = Mutex()
        private val channel = BroadcastChannel<T>(Channel.BUFFERED)

        init {
            originalScope.launch {
                for (item in this@asCachingStream) {
                    bufferMutex.withLock {
                        if (channels.isEmpty()) {
                            buffer.add(item)
                        } else {
                            channel.send(item)
                        }
                    }
                }
            }
        }

        override fun observe(lambda: (T) -> Unit): Stream.Closeable {
            val scope = CoroutineScope(Dispatchers.Unconfined)
            val receiveChannel = channel.openSubscription()

            scope.launch {
                bufferMutex.withLock {
                    channels += receiveChannel
                    buffer.forEach { buffered ->
                        channel.send(buffered)
                    }
                    buffer.clear()
                }
                for (item in receiveChannel) {
                    lambda(item)
                }
            }
            return object : Stream.Closeable {
                override fun close() {
                    runBlocking {
                        bufferMutex.withLock {
                            channels.remove(receiveChannel)
                            receiveChannel.cancel()
                        }
                    }
                }
            }
        }
    }
}
