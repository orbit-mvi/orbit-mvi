/*
 * Copyright 2021-2023 Mikołaj Leszczyński & Appmattus Limited
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.internal.repeatonsubscription.DelayingSubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.refCount
import org.orbitmvi.orbit.syntax.ContainerContext
import kotlin.coroutines.EmptyCoroutineContext

public class RealContainer<STATE : Any, SIDE_EFFECT : Any> constructor(
    initialState: STATE,
    parentScope: CoroutineScope,
    public override val settings: RealSettings,
    subscribedCounterOverride: SubscribedCounter? = null,
    public override val containerHostName: String? = null
) : Container<STATE, SIDE_EFFECT> {
    private val scope = parentScope + settings.eventLoopDispatcher
    private val dispatchChannel = Channel<suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit>(Channel.UNLIMITED)
    private val initialised = atomic(false)

    private val subscribedCounter = subscribedCounterOverride ?: DelayingSubscribedCounter(scope, settings.repeatOnSubscribedStopTimeout)

    private inner class FlowMetadata<T>(val intentName: String?, val value: T)

    private val internalStateFlow = MutableStateFlow(FlowMetadata("${containerHostName ?: "<not-populated>"}.<initial-state>", initialState))

    override val stateFlow: StateFlow<STATE> =
        internalStateFlow.logState().map { it.value }.stateIn(scope, SharingStarted.Eagerly, initialState).refCount(subscribedCounter)

    private val sideEffectChannel = Channel<FlowMetadata<SIDE_EFFECT>>(settings.sideEffectBufferSize)

    override val sideEffectFlow: Flow<SIDE_EFFECT> =
        sideEffectChannel.receiveAsFlow().logSideEffect().map { it.value }.refCount(subscribedCounter)

    private fun StateFlow<FlowMetadata<STATE>>.logState(): Flow<FlowMetadata<STATE>> = onEach { metadata ->
        settings.loggers.forEach { logger ->
            logger.logState(containerHostName ?: "<not-populated>", metadata.intentName ?: "<not-populated>", metadata.value)
        }
    }

    private fun Flow<FlowMetadata<SIDE_EFFECT>>.logSideEffect(): Flow<FlowMetadata<SIDE_EFFECT>> = onEach { metadata ->
        settings.loggers.forEach { logger ->
            logger.logSideEffect(containerHostName ?: "<not-populated>", metadata.intentName ?: "<not-populated>", metadata.value)
        }
    }

    internal val pluginContext: ContainerContext<STATE, SIDE_EFFECT> = ContainerContext(
        settings = settings,
        postSideEffect = { intentName, sideEffect -> sideEffectChannel.send(FlowMetadata(intentName, sideEffect)) },
        getState = { internalStateFlow.value.value },
        reduce = { intentName, reducer -> internalStateFlow.update { FlowMetadata(intentName, reducer(it.value)) } },
        subscribedCounter
    )

    override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        initialiseIfNeeded()
        dispatchChannel.send(orbitIntent)
    }

    @OrbitExperimental
    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        initialiseIfNeeded()
        pluginContext.orbitIntent()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun initialiseIfNeeded() {
        if (initialised.compareAndSet(expect = false, update = true)) {
            scope.produce<Unit>(Dispatchers.Unconfined) {
                awaitClose {
                    settings.idlingRegistry.close()
                }
            }
            scope.launch {
                val exceptionHandlerContext = settings.exceptionHandler?.plus(SupervisorJob(scope.coroutineContext[Job]))
                val context = settings.intentLaunchingDispatcher + (exceptionHandlerContext ?: EmptyCoroutineContext)

                for (msg in dispatchChannel) {
                    launch(context) { pluginContext.msg() }
                }
            }
        }
    }
}
