package org.orbitmvi.orbit.test

import app.cash.turbine.ReceiveTurbine
import kotlinx.atomicfu.atomic
import org.orbitmvi.orbit.ContainerHost
import kotlin.test.assertEquals
import kotlin.test.fail

public abstract class BaseTestContainerHost<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: CONTAINER_HOST,
    initialState: STATE?,
    private val emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>>
) : OrbitTestContext<STATE, SIDE_EFFECT> {

    private val onCreateAllowed = atomic(true)

    private lateinit var _previousState: STATE

    private var upcomingState: STATE? = null

    public override val previousState: STATE
        get() = _previousState

    private val resolvedInitialState: STATE by lazy { initialState ?: actual.container.findTestContainer().originalInitialState }

    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    protected fun runOnCreateInternal() {
        if (!onCreateAllowed.compareAndSet(expect = true, update = false)) {
            error("runOnCreate should only be invoked once and before any testIntent call")
        }

        actual.container.findOnCreate().invoke(resolvedInitialState)
    }

    /**
     * Invoke an intent on the [ContainerHost] under test.
     */
    public fun invokeIntentInternal(action: CONTAINER_HOST.() -> Unit) {
        onCreateAllowed.lazySet(false)
        actual.action()
    }

    override suspend fun awaitItem(): Item<STATE, SIDE_EFFECT> {
        upcomingState?.let { _previousState = it }
        val upcoming = emissions.awaitItem()

        if (upcoming is Item.StateItem) {
            upcomingState = upcoming.value
        }
        return upcoming
    }

    public override suspend fun awaitState(): STATE {
        upcomingState?.let { _previousState = it }
        val item = awaitItem()

        return (item as? Item.StateItem)?.value.also { upcomingState = it } ?: fail("Expected State but got $item")
    }

    public override suspend fun awaitSideEffect(): SIDE_EFFECT {
        val item = awaitItem()

        return (item as? Item.SideEffectItem)?.value ?: fail("Expected Side Effect but got $item")
    }

    override suspend fun cancel() {
        emissions.cancel()
    }

    override suspend fun cancelAndIgnoreRemainingEvents() {
        emissions.cancelAndIgnoreRemainingEvents()
    }

    override suspend fun expectInitialState() {
        assertEquals(resolvedInitialState, awaitState())
    }

    override fun expectMostRecentItem(): Item<STATE, SIDE_EFFECT> {
        return emissions.expectMostRecentItem()
    }

    override suspend fun skipItems(count: Int) {
        emissions.skipItems(count)
    }
}
