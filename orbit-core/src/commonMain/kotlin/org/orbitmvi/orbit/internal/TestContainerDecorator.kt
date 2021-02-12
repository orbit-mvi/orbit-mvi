package org.orbitmvi.orbit.internal

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin

public class TestContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    private val parentScope: CoroutineScope,
    override val actual: Container<STATE, SIDE_EFFECT>
) : ContainerDecorator<STATE, SIDE_EFFECT> {
    private val testMode = atomic(false)
    private val testContainer = atomic<TestContainer<STATE, SIDE_EFFECT>?>(null)
    private val delegate: Container<STATE, SIDE_EFFECT>
        get() = if (testMode.value) {
            testContainer.value!!
        } else {
            actual
        }

    override val currentState: STATE
        get() = delegate.currentState

    override val stateFlow: Flow<STATE>
        get() = delegate.stateFlow

    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = delegate.sideEffectFlow

    override fun orbit(orbitFlow: suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        delegate.orbit(orbitFlow)
    }

    fun test(
        initialState: STATE,
        isolateFlow: Boolean = true,
        blocking: Boolean = true
    ) {
        testMode.compareAndSet(expect = false, update = true)
        testContainer.update {
            TestContainer(
                initialState = initialState,
                parentScope = parentScope,
                isolateFlow = isolateFlow,
                blocking = blocking
            )
        }
    }
}