package org.orbitmvi.orbit.internal

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.syntax.ContainerContext

public class TestContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    private val parentScope: CoroutineScope,
    override val actual: Container<STATE, SIDE_EFFECT>
) : ContainerDecorator<STATE, SIDE_EFFECT> {

    private val delegate = atomic(actual)

    public val savedFlows: Channel<(suspend () -> Unit)>
        get() = (delegate.value as FlowSavingContainerDecorator).savedFlows

    override val settings: Container.Settings
        get() = delegate.value.settings

    override val stateFlow: StateFlow<STATE>
        get() = delegate.value.stateFlow

    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = delegate.value.sideEffectFlow

    override suspend fun orbit(orbitFlow: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        delegate.value.orbit(orbitFlow)
    }

    public fun test(
        initialState: STATE,
        strategy: TestingStrategy
    ) {
        val newContainer: Container<STATE, SIDE_EFFECT> = RealContainer(
            initialState = initialState,
            parentScope = parentScope,
            settings = if (strategy is TestingStrategy.Live) strategy.settings else actual.settings
        )

        val newDelegate = when (strategy) {
            is TestingStrategy.Suspending -> FlowSavingContainerDecorator(newContainer)
            is TestingStrategy.Live -> newContainer
        }

        val testDispatcherSet = delegate.compareAndSet(
            expect = actual,
            update = newDelegate
        )

        if (!testDispatcherSet) {
            throw IllegalStateException("Can only call test() once")
        }
    }
}

public sealed class TestingStrategy {

    public object Suspending : TestingStrategy()

    public class Live(
        internal val settings: Container.Settings
    ) : TestingStrategy()
}

public class FlowSavingContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    override val actual: Container<STATE, SIDE_EFFECT>
) : ContainerDecorator<STATE, SIDE_EFFECT> {

    private val dispatched = atomic<Boolean>(false)

    public val savedFlows: Channel<suspend () -> Unit> = Channel()

    override suspend fun orbit(orbitFlow: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
//        if (dispatched.compareAndSet(expect = false, update = true)) {
//        val mutex = Mutex(locked = true)
        actual.orbit {
            val payload: suspend () -> Unit = { orbitFlow() }
            savedFlows.send(payload)
//            mutex.unlock()
        }
//        runBlocking {
//            mutex.withLock { }
//        }
//        }
    }
}
