/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
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
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.internal.repeatonsubscription.DelayingSubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.refCount
import org.orbitmvi.orbit.syntax.ContainerContext
import kotlin.coroutines.EmptyCoroutineContext

public class RealContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    parentScope: CoroutineScope,
    public override val settings: RealSettings,
    subscribedCounterOverride: SubscribedCounter? = null
) : Container<STATE, SIDE_EFFECT> {
    private val scope = parentScope + settings.eventLoopDispatcher
    private val intentJob = Job(scope.coroutineContext[Job])
    private val dispatchChannel = Channel<Pair<CompletableJob, suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit>>(Channel.UNLIMITED)
    private val initialised = atomic(false)
    private val subscribedCounter = subscribedCounterOverride ?: DelayingSubscribedCounter(scope, settings.repeatOnSubscribedStopTimeout)
    private val internalStateFlow = MutableStateFlow(initialState)
    private val sideEffectChannel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)
    private val intentCounter = atomic(0)

    override val stateFlow: StateFlow<STATE> = internalStateFlow.asStateFlow()
    override val sideEffectFlow: Flow<SIDE_EFFECT> = sideEffectChannel.receiveAsFlow()

    override val refCountStateFlow: StateFlow<STATE> = internalStateFlow.refCount(subscribedCounter)
    override val refCountSideEffectFlow: Flow<SIDE_EFFECT> = sideEffectFlow.refCount(subscribedCounter)

    override suspend fun joinIntents() {
        intentJob.children.toList().joinAll()
    }

    override fun cancel() {
        scope.cancel()
        intentJob.cancel()
    }

    internal val pluginContext: ContainerContext<STATE, SIDE_EFFECT> = ContainerContext(
        settings = settings,
        postSideEffect = { sideEffectChannel.send(it) },
        getState = { internalStateFlow.value },
        reduce = { reducer -> internalStateFlow.update(reducer) },
        subscribedCounter,
    )

    override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit): Job {
        initialiseIfNeeded()

        val job = Job(intentJob)
        dispatchChannel.send(job to orbitIntent)
        return job
    }

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

            scope.launch(CoroutineName(COROUTINE_NAME_EVENT_LOOP)) {
                for ((job, intent) in dispatchChannel) {
                    val exceptionHandlerContext =
                        (settings.exceptionHandler?.plus(SupervisorJob(job)) ?: job) +
                            (settings.intentLaunchingDispatcher ?: EmptyCoroutineContext) +
                            CoroutineName("$COROUTINE_NAME_INTENT${intentCounter.getAndIncrement()}")
                    launch(exceptionHandlerContext, start = CoroutineStart.UNDISPATCHED) {
                        pluginContext.intent()
                    }.invokeOnCompletion { job.complete() }
                }
            }
        }
    }

    private companion object {
        private const val COROUTINE_NAME_EVENT_LOOP = "orbit-event-loop"
        private const val COROUTINE_NAME_INTENT = "orbit-intent-"
    }
}
