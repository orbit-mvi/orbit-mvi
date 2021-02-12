package org.orbitmvi.orbit.internal

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.test.runBlocking
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin

internal class TestContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    parentScope: CoroutineScope,
    private val isolateFlow: Boolean,
    private val blocking: Boolean
) : RealContainer<STATE, SIDE_EFFECT>(
    initialState = initialState,
    parentScope = parentScope + Dispatchers.Unconfined,
    settings = Container.Settings(
        orbitDispatcher =
        @Suppress("EXPERIMENTAL_API_USAGE") if (blocking) Dispatchers.Unconfined else Dispatchers.Default,
        backgroundDispatcher = Dispatchers.Unconfined
    )
) {
    private val dispatched = atomic<Int>(0)

    override fun orbit(orbitFlow: suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        if (!isolateFlow || dispatched.compareAndSet(0, 1)) {
            if (blocking) {
                runBlocking {
                    orbitFlow(pluginContext)
                }
            } else {
                super.orbit(orbitFlow)
            }
        }
    }
}
