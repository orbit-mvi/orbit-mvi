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

import hu.akarnokd.kotlin.flow.replay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executors

@FlowPreview
open class RealContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    settings: Container.Settings,
    orbitDispatcher: CoroutineDispatcher = DEFAULT_DISPATCHER,
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
) : Container<STATE, SIDE_EFFECT> {
    override val currentState: STATE
        get() = stateChannel.value
    private val stateChannel = ConflatedBroadcastChannel(initialState)
    private val sideEffectChannel = Channel<SIDE_EFFECT>(Channel.RENDEZVOUS)
    private val scope = CoroutineScope(orbitDispatcher)
    private val stateMutex = Mutex()
    private val sideEffectMutex = Mutex()

    override val orbit: Stream<STATE> =
        stateChannel.asFlow().distinctUntilChanged().replay(1) { it }.asStream()

    override val sideEffect: Stream<SIDE_EFFECT> =
        if (settings.sideEffectCaching) {
            sideEffectChannel.asCachingStream(scope)
        } else {
            sideEffectChannel.asStream(scope)
        }

    override fun <EVENT : Any> orbit(
        event: EVENT,
        init: Builder<STATE, SIDE_EFFECT, EVENT>.() -> Builder<STATE, SIDE_EFFECT, *>
    ) {
        scope.launch {
            collectFlow(
                event,
                init
            )
        }
    }

    private val pluginContext = OrbitPlugin.ContainerContext<STATE, SIDE_EFFECT>(
        backgroundDispatcher = backgroundDispatcher,
        setState = {
            scope.launch {
                stateMutex.withLock {
                    val reduced = it()
                    stateChannel.send(reduced)
                }
            }.join()
        },
        postSideEffect = { event: SIDE_EFFECT ->
            scope.launch {
                sideEffectMutex.withLock {
                    sideEffectChannel.send(event)
                }
            }
        }
    )

    @Suppress("UNCHECKED_CAST")
    suspend fun <EVENT : Any> collectFlow(
        event: EVENT,
        init: Builder<STATE, SIDE_EFFECT, EVENT>.() -> Builder<STATE, SIDE_EFFECT, *>
    ) {
        Builder<STATE, SIDE_EFFECT, EVENT>()
            .init().stack.fold(flowOf(event)) { flow: Flow<Any>, operator: Operator<STATE, *> ->
                Orbit.plugins.fold(flow) { flow2: Flow<Any>, plugin: OrbitPlugin ->
                    plugin.apply(
                        pluginContext,
                        flow2,
                        operator as Operator<STATE, Any>
                    ) { Context(currentState, it) }
                }
            }.collect()
    }

    companion object {
        private val DEFAULT_DISPATCHER by lazy {
            Executors.newSingleThreadExecutor { Thread(it, "orbit") }.asCoroutineDispatcher()
        }
    }
}
