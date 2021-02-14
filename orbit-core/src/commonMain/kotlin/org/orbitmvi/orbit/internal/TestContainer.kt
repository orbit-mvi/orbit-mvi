package org.orbitmvi.orbit.internal

import kotlinx.atomicfu.atomic
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
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin
import org.orbitmvi.orbit.test.runBlocking

internal class TestContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    parentScope: CoroutineScope,
    private val isolateFlow: Boolean,
    private val blocking: Boolean
) : Container<STATE, SIDE_EFFECT> {

    val settings = Container.Settings(
        orbitDispatcher =
        @Suppress("EXPERIMENTAL_API_USAGE") if (blocking) Dispatchers.Unconfined else Dispatchers.Default,
        backgroundDispatcher = Dispatchers.Unconfined
    )

    private val scope = parentScope + settings.orbitDispatcher
    private val dispatchChannel = Channel<suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit>(Channel.UNLIMITED)
    private val mutex = Mutex()
    private val dispatched = atomic<Int>(0)

    private val internalStateFlow = MutableStateFlow(initialState)
    override val currentState: STATE
        get() = internalStateFlow.value
    override val stateFlow: Flow<STATE> = internalStateFlow

    private val sideEffectChannel = Channel<SIDE_EFFECT>(settings.sideEffectBufferSize)
    override val sideEffectFlow: Flow<SIDE_EFFECT> = sideEffectChannel.receiveAsFlow()

    private val pluginContext: OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT> = OrbitDslPlugin.ContainerContext(
        settings = settings,
        postSideEffect = { sideEffectChannel.send(it) },
        getState = { internalStateFlow.value },
        reduce = { reducer ->
            mutex.withLock {
                internalStateFlow.value = reducer(internalStateFlow.value)
            }
        }
    )

    init {
        @Suppress("EXPERIMENTAL_API_USAGE")
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
        if (!isolateFlow || dispatched.compareAndSet(0, 1)) {
            if (blocking) {
                runBlocking {
                    orbitFlow(pluginContext)
                }
            } else {
                dispatchChannel.offer(orbitFlow)
            }
        }
    }
}
