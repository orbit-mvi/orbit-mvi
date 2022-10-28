package org.orbitmvi.orbit.test

import app.cash.turbine.ReceiveTurbine
import org.orbitmvi.orbit.ContainerHost

public class RegularTestContainerHost<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    actual: CONTAINER_HOST,
    initialState: STATE?,
    emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>>
) : BaseTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST>(
    actual, initialState, emissions
) {

    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    public fun runOnCreate() {
        super.runOnCreateInternal()
    }

    /**
     * Invoke an intent on the [ContainerHost] under test.
     */
    public fun invokeIntent(action: CONTAINER_HOST.() -> Unit) {
        super.invokeIntentInternal(action)
    }
}
