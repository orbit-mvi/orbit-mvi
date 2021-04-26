package org.orbitmvi.orbit.internal

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
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

    override val stateFlow: StateFlow<STATE>
        get() = delegate.value.stateFlow

    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = delegate.value.sideEffectFlow

    override fun orbit(orbitFlow: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        delegate.value.orbit(orbitFlow)
    }

    public fun test(
        initialState: STATE,
        isolateFlow: Boolean = true,
        blocking: Boolean = true
    ) {
        val testDispatcherSet = delegate.compareAndSet(
            expect = actual,
            update = TestContainer(
                initialState = initialState,
                parentScope = parentScope,
                isolateFlow = isolateFlow,
                blocking = blocking
            )
        )

        if (!testDispatcherSet) {
            throw IllegalStateException("Can only call test() once")
        }
    }
}
