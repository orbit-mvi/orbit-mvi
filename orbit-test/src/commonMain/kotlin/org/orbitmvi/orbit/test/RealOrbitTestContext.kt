package org.orbitmvi.orbit.test

import app.cash.turbine.ReceiveTurbine
import kotlinx.atomicfu.atomic
import org.orbitmvi.orbit.ContainerHost
import kotlin.test.assertEquals
import kotlin.test.fail

public class RealOrbitTestContext<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: CONTAINER_HOST,
    initialState: STATE?,
    private val emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>>
) : OrbitTestContext<STATE, SIDE_EFFECT, CONTAINER_HOST> {

    private val onCreateAllowed = atomic(true)

    private var upcomingState: STATE? = null

    private val resolvedInitialState: STATE by lazy { initialState ?: actual.container.findTestContainer().originalInitialState }

    override fun runOnCreate() {
        if (onCreateAllowed.compareAndSet(expect = true, update = false)) {
            actual.container.findOnCreate().invoke(resolvedInitialState)
        } else {
            error("runOnCreate should only be invoked once and before any invokeIntent call")
        }
    }

    override fun invokeIntent(action: CONTAINER_HOST.() -> Unit) {
        onCreateAllowed.lazySet(false)
        actual.action()
    }

    override suspend fun awaitItem(): Item<STATE, SIDE_EFFECT> {
        return emissions.awaitItem()
    }

    public override suspend fun awaitState(): STATE {
        val item = awaitItem()

        return (item as? Item.StateItem)?.value.also { upcomingState = it } ?: fail("Expected State but got $item")
    }

    public override suspend fun awaitSideEffect(): SIDE_EFFECT {
        val item = awaitItem()

        return (item as? Item.SideEffectItem)?.value ?: fail("Expected Side Effect but got $item")
    }

    override suspend fun cancelAndIgnoreRemainingItems() {
        emissions.cancelAndIgnoreRemainingEvents()
    }

    override suspend fun expectInitialState() {
        assertEquals(resolvedInitialState, awaitState())
    }

    override suspend fun skipItems(count: Int) {
        emissions.skipItems(count)
    }
}
