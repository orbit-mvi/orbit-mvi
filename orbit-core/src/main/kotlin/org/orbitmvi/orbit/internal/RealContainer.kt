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

package org.orbitmvi.orbit.internal

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Suppress("EXPERIMENTAL_API_USAGE")
public open class RealContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    parentScope: CoroutineScope,
    private val settings: Container.Settings
) : Container<STATE, SIDE_EFFECT> {
    private val scope = parentScope + settings.orbitDispatcher
    private val dispatchChannel = Channel<suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit>(Channel.UNLIMITED)
    private val mutex = Mutex()

    private val internalStateFlow = MutableStateFlow(initialState)
    override val currentState: STATE
        get() = internalStateFlow.value
    override val stateFlow: Flow<STATE> = internalStateFlow

    private val sideEffectChannel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)
    override val sideEffectFlow: Flow<SIDE_EFFECT> = sideEffectChannel.receiveAsFlow()

    protected val pluginContext: OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT> = OrbitDslPlugin.ContainerContext(
        settings = settings,
        postSideEffect = { sideEffectChannel.send(it) },
        getState = {
            internalStateFlow.value
        },
        reduce = { reducer ->
            mutex.withLock {
                internalStateFlow.value = reducer(internalStateFlow.value)
            }
        }
    )

    init {
        scope.produce<Unit>(Dispatchers.Unconfined) {
            awaitClose {
                settings.idlingRegistry.close()
            }
        }
        scope.launch {
            for (msg in dispatchChannel) {
                launch(Dispatchers.Unconfined) { pluginContext.msg() }
            }
        }
    }

    override fun orbit(orbitFlow: suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        dispatchChannel.offer(orbitFlow)
    }
}
