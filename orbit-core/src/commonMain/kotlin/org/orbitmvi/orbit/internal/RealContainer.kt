/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.internal

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.ContainerContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("EXPERIMENTAL_API_USAGE")
public class RealContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    parentScope: CoroutineScope,
    public override val settings: Container.Settings
) : Container<STATE, SIDE_EFFECT> {
    private val scope = parentScope + settings.intentLoopDispatcher
    private val dispatchChannel = Channel<suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit>(Channel.UNLIMITED)
    private val mutex = Mutex()
    private val initialised = atomic(false)

    private val internalStateFlow = MutableStateFlow(initialState)
    override val stateFlow: StateFlow<STATE> = internalStateFlow.asStateFlow()

    private val sideEffectChannel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)
    override val sideEffectFlow: Flow<SIDE_EFFECT> = sideEffectChannel.receiveAsFlow()

    internal val pluginContext: ContainerContext<STATE, SIDE_EFFECT> = ContainerContext(
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

    override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        initialiseIfNeeded()
        dispatchChannel.send(orbitIntent)
    }

    private fun initialiseIfNeeded() {
        if (initialised.compareAndSet(expect = false, update = true)) {
            scope.produce<Unit>(Dispatchers.Unconfined) {
                awaitClose {
                    settings.idlingRegistry.close()
                }
            }
            scope.launch {
                val context = Dispatchers.Unconfined + (settings.exceptionHandler?.plus(SupervisorJob()) ?: EmptyCoroutineContext)

                for (msg in dispatchChannel) {
                    launch(context) { pluginContext.msg() }
                }
            }
        }
    }
}
