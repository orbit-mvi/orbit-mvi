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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Suppress("EXPERIMENTAL_API_USAGE")
open class RealContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    private val settings: Container.Settings,
    orbitDispatcher: CoroutineDispatcher = DEFAULT_DISPATCHER,
    backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO,
    parentScope: CoroutineScope
) : Container<STATE, SIDE_EFFECT> {
    private val scope = parentScope + orbitDispatcher
    private val internalStateFlow = MutableStateFlow(initialState)
    private val sideEffectChannel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)
    private val sideEffectMutex = Mutex()
    private val pluginContext = OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>(
        backgroundDispatcher = backgroundDispatcher,
        setState = { internalStateFlow.value = it },
        postSideEffect = { event: SIDE_EFFECT ->
            scope.launch {
                // Ensure side effect ordering
                sideEffectMutex.withLock {
                    sideEffectChannel.send(event)
                }
            }
        },
        settings = settings
    )

    init {
        scope.produce<Unit> {
            awaitClose {
                settings.idlingRegistry.close()
            }
        }
    }

    override val currentState: STATE
        get() = internalStateFlow.value

    override val stateFlow = internalStateFlow

    override val sideEffectFlow: Flow<SIDE_EFFECT> get() = sideEffectChannel.receiveAsFlow()

    override val stateStream = stateFlow.asStream()

    override val sideEffectStream = sideEffectFlow.asStream()

    override fun orbit(init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>) {
        scope.launch {
            collectFlow(init)
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun collectFlow(init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>) {
        Builder<STATE, SIDE_EFFECT, Unit>()
            .init().stack.fold(flowOf(Unit)) { flow: Flow<Any?>, operator: Operator<STATE, *> ->
                OrbitDslPlugins.plugins.fold(flow) { flow2: Flow<Any?>, plugin: OrbitDslPlugin ->
                    plugin.apply(
                        pluginContext,
                        flow2,
                        operator as Operator<STATE, Any?>
                    ) {
                        object : VolatileContext<STATE, Any?> {
                            override val state = currentState
                            override val event = it
                            override fun volatileState() = currentState
                        }
                    }
                }
            }.collect()
    }

    companion object {
        // To be replaced by the new API when it hits:
        // https://github.com/Kotlin/kotlinx.coroutines/issues/261
        @Suppress("EXPERIMENTAL_API_USAGE")
        private val DEFAULT_DISPATCHER by lazy {
            newSingleThreadContext("orbit")
        }
    }
}
