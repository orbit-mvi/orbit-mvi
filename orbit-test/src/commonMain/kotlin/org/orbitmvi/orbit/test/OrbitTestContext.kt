/*
 * Copyright 2023-2025 Mikołaj Leszczyński & Appmattus Limited
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
 */

package org.orbitmvi.orbit.test

import app.cash.turbine.ReceiveTurbine
import kotlinx.coroutines.Job
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitInternal
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

public class OrbitTestContext<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    public val containerHost: CONTAINER_HOST,
    private val resolvedInitialState: STATE,
    private val emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>>,
    private val settings: TestSettings
) {
    @PublishedApi
    internal var currentConsumedState: STATE = resolvedInitialState

    /**
     * Invoke `onCreate` lambda for the [ContainerHost].
     */
    public fun runOnCreate(): Job {
        @OptIn(OrbitInternal::class)
        val onCreate = containerHost.container.findOnCreate()
        @OptIn(OrbitInternal::class)
        return containerHost.container.orbit(onCreate)
    }

    /**
     * Sanity check assertion. Checks if the initial state is emitted and matches
     * the initial state defined for the production container (or the one specified
     * in the test).
     *
     * @throws AssertionError if initial state does not match.
     */
    @Deprecated(
        "Initial state is now checked automatically. If you wish to manually verify the initial state, use " +
            "`autoCheckInitialState=false` in test settings and use `expectState` or `awaitState`"
    )
    public suspend fun expectInitialState() {
        if (settings.autoCheckInitialState) {
            println(
                "Initial state is now checked automatically. If you wish to manually verify the initial state, use " +
                    "`autoCheckInitialState=false` in test settings and use `expectState` or `awaitState`"
            )
        } else {
            assertEquals(resolvedInitialState, awaitState())
        }
    }

    /**
     * Awaits for a state and checks if it matches the expected state change from the
     * previous state.
     *
     * @param expectedChange The expected change from the previous state.
     *
     * @throws AssertionError if state does not match the expected change.
     */
    public suspend fun expectState(expectedChange: STATE.() -> STATE) {
        assertEquals(expectedChange(currentConsumedState), awaitState())
    }

    /**
     * Awaits for a state and checks if it matches the expected state.
     *
     * @param expected The expected state.
     *
     * @throws AssertionError if state does not match the expected state.
     */
    public suspend fun expectState(expected: STATE) {
        assertEquals(expected, awaitState())
    }

    /**
     * Awaits for a state and checks if it matches the expected state change from the
     * previous state, ensuring the last state is of the expected type.
     *
     * @param expectedChange The expected change from the previous state.
     *
     * @throws AssertionError if state does not match the expected change.
     */
    public suspend inline fun <reified LAST_STATE : STATE> expectStateOn(expectedChange: LAST_STATE.() -> STATE) {
        val currentConsumedState = currentConsumedState
        assertIs<LAST_STATE>(currentConsumedState)
        assertEquals(expectedChange(currentConsumedState), awaitState())
    }

    /**
     * Awaits for a side effect and checks if it matches the expected side effect
     *
     * @param expected The expected side effect
     *
     * @throws AssertionError if state does not match the expected change.
     */
    public suspend fun expectSideEffect(expected: SIDE_EFFECT) {
        assertEquals(expected, awaitSideEffect())
    }

    /**
     * Assert there are no unconsumed items
     *
     * @throws AssertionError if unconsumed items are found
     */
    public suspend fun expectNoItems() {
        emissions.expectNoEvents()
    }

    /**
     * Return the next item received.
     * This function will suspend if no items have been received.
     */
    public suspend fun awaitItem(): Item<STATE, SIDE_EFFECT> {
        return emissions.awaitItem()
    }

    /**
     * Return the next state received.
     * This function will suspend if no states have been received.
     *
     * @throws AssertionError if the most recent item was not a state.
     */
    public suspend fun awaitState(): STATE {
        val item = awaitItem()
        return (item as? Item.StateItem)?.value?.also { currentConsumedState = it }
            ?: fail("Expected State but got $item")
    }

    /**
     * Return the next side effect received.
     * This function will suspend if no side effects have been received.
     *
     * @throws AssertionError if the most recent item was not a side effect.
     */
    public suspend fun awaitSideEffect(): SIDE_EFFECT {
        val item = awaitItem()

        return (item as? Item.SideEffectItem)?.value ?: fail("Expected Side Effect but got $item")
    }

    /**
     * Assert that [count] items were received and ignore them.
     * This function will suspend if no items have been received.
     */
    public suspend fun skipItems(count: Int) {
        emissions.skipItems(count)
    }

    /**
     * Finish this test and ignore any events which have already been received.
     * This also cancels any in-progress intents.
     */
    public suspend fun cancelAndIgnoreRemainingItems() {
        emissions.cancelAndIgnoreRemainingEvents()
        containerHost.container.cancel()
    }
}
