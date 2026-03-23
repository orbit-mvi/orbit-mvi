/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.internal.repeatonsubscription.DelayingSubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.refCount
import org.orbitmvi.orbit.syntax.ContainerContext
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.fetchAndIncrement

public class RealContainer<INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any>(
    initialState: INTERNAL_STATE,
    parentScope: CoroutineScope,
    public override val settings: RealSettings,
    internal val transformState: (INTERNAL_STATE) -> EXTERNAL_STATE,
    subscribedCounterOverride: SubscribedCounter? = null
) : OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
    override val scope: CoroutineScope = parentScope + settings.eventLoopDispatcher
    private val intentJob = Job(scope.coroutineContext[Job])
    private val dispatchChannel =
        Channel<Pair<CompletableJob, suspend ContainerContext<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit>>(
            Channel.UNLIMITED
        )
    private val initialised = AtomicBoolean(false)
    private val subscribedCounter = subscribedCounterOverride ?: DelayingSubscribedCounter(scope, settings.repeatOnSubscribedStopTimeout)
    private val internalStateFlow = MutableStateFlow(initialState)
    private val sideEffectChannel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)
    private val intentCounter = AtomicInt(0)

    override val stateFlow: StateFlow<INTERNAL_STATE> = internalStateFlow.asStateFlow()
    override val sideEffectFlow: Flow<SIDE_EFFECT> = sideEffectChannel.receiveAsFlow()

    override val refCountStateFlow: StateFlow<INTERNAL_STATE> = internalStateFlow.refCount(subscribedCounter)
    override val refCountSideEffectFlow: Flow<SIDE_EFFECT> = sideEffectFlow.refCount(subscribedCounter)

    override val externalStateFlow: StateFlow<EXTERNAL_STATE> by lazy {
        stateFlow.stateMap(transformState)
    }
    override val externalRefCountStateFlow: StateFlow<EXTERNAL_STATE> by lazy {
        refCountStateFlow.stateMap(transformState)
    }

    override suspend fun joinIntents() {
        intentJob.children.toList().joinAll()
    }

    override fun cancel() {
        scope.cancel()
        intentJob.cancel()
    }

    internal val pluginContext: ContainerContext<INTERNAL_STATE, SIDE_EFFECT> = ContainerContext(
        settings = settings,
        postSideEffect = { sideEffectChannel.send(it) },
        reduce = { reducer -> internalStateFlow.update(reducer) },
        subscribedCounter = subscribedCounter,
        stateFlow = stateFlow,
    )

    override fun orbit(orbitIntent: suspend ContainerContext<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit): Job {
        initialiseIfNeeded()

        val job = Job(intentJob)
        dispatchChannel.trySend(job to orbitIntent)
        return job
    }

    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<INTERNAL_STATE, SIDE_EFFECT>.() -> Unit) {
        initialiseIfNeeded()
        pluginContext.orbitIntent()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun initialiseIfNeeded() {
        if (initialised.compareAndSet(expectedValue = false, newValue = true)) {
            scope.produce<Unit>(Dispatchers.Unconfined) {
                awaitClose {
                    settings.idlingRegistry.close()
                }
            }

            scope.launch(CoroutineName(COROUTINE_NAME_EVENT_LOOP)) {
                for ((job, intent) in dispatchChannel) {
                    val exceptionHandlerContext =
                        CoroutineName("$COROUTINE_NAME_INTENT${intentCounter.fetchAndIncrement()}") +
                            job +
                            settings.intentLaunchingDispatcher
                    launch(exceptionHandlerContext) {
                        runCatching { pluginContext.intent() }.onFailure { e ->
                            settings.exceptionHandler?.handleException(coroutineContext, e) ?: throw e
                        }
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

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
internal class MappedStateFlow<T : Any, U : Any>(
    private val upstream: StateFlow<T>,
    private val transform: (T) -> U
) : StateFlow<U> {

    override val replayCache: List<U>
        get() = upstream.replayCache.map(transform)

    override val value: U
        get() = transform(upstream.value)

    override suspend fun collect(collector: FlowCollector<U>): Nothing {
        var previous: Any? = null
        upstream.collect {
            val value = transform(it)
            if (previous == null || value != previous) {
                previous = value
                collector.emit(value)
            }
        }
    }
}

internal fun <T : Any, U : Any> StateFlow<T>.stateMap(transform: (T) -> U): StateFlow<U> =
    MappedStateFlow(this, transform)
