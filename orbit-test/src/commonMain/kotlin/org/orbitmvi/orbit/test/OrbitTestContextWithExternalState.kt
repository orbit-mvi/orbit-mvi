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
import org.orbitmvi.orbit.ContainerHostWithExternalState
import org.orbitmvi.orbit.annotation.OrbitInternal
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

public class OrbitTestContextWithExternalState<INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>>(
    public val containerHost: CONTAINER_HOST,
    private val resolvedInitialState: INTERNAL_STATE,
    private val emissions: ReceiveTurbine<ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>>,
    private val settings: TestSettings
) {
    @PublishedApi
    internal var currentConsumedInternalState: INTERNAL_STATE = resolvedInitialState

    @PublishedApi
    internal var currentConsumedExternalState: EXTERNAL_STATE = containerHost.mapToExternalState(resolvedInitialState)

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
     * Awaits for an internal state and checks if it matches the expected internal state change from the
     * previous internal state.
     *
     * @param expectedChange The expected change from the previous internal state.
     *
     * @throws AssertionError if internal state does not match the expected change.
     */
    public suspend fun expectInternalState(expectedChange: INTERNAL_STATE.() -> INTERNAL_STATE) {
        assertEquals(expectedChange(currentConsumedInternalState), awaitInternalState())
    }

    /**
     * Awaits for an internal state and checks if it matches the expected internal state.
     *
     * @param expected The expected internal state.
     *
     * @throws AssertionError if internal state does not match the expected internal state.
     */
    public suspend fun expectInternalState(expected: INTERNAL_STATE) {
        assertEquals(expected, awaitInternalState())
    }

    /**
     * Awaits for an internal state and checks if it matches the expected internal state change from the
     * previous internal state, ensuring the last internal state is of the expected type.
     *
     * @param expectedChange The expected change from the previous internal state.
     *
     * @throws AssertionError if internal state does not match the expected change.
     */
    public suspend inline fun <reified LAST_STATE : INTERNAL_STATE> expectInternalStateOn(expectedChange: LAST_STATE.() -> INTERNAL_STATE) {
        val currentConsumedState = currentConsumedInternalState
        assertIs<LAST_STATE>(currentConsumedState)
        assertEquals(expectedChange(currentConsumedState), awaitInternalState())
    }

    /**
     * Awaits for an external state and checks if it matches the expected external state change from the
     * previous external state.
     *
     * @param expectedChange The expected change from the previous external state.
     *
     * @throws AssertionError if external state does not match the expected change.
     */
    public suspend fun expectExternalState(expectedChange: EXTERNAL_STATE.() -> EXTERNAL_STATE) {
        assertEquals(expectedChange(currentConsumedExternalState), awaitExternalState())
    }

    /**
     * Awaits for an external state and checks if it matches the expected external state.
     *
     * @param expected The expected external state.
     *
     * @throws AssertionError if external state does not match the expected external state.
     */
    public suspend fun expectExternalState(expected: EXTERNAL_STATE) {
        assertEquals(expected, awaitExternalState())
    }

    /**
     * Awaits for an external state and checks if it matches the expected external state change from the
     * previous external state, ensuring the last external state is of the expected type.
     *
     * @param expectedChange The expected change from the previous external state.
     *
     * @throws AssertionError if external state does not match the expected change.
     */
    public suspend inline fun <reified LAST_STATE : EXTERNAL_STATE> expectExternalStateOn(expectedChange: LAST_STATE.() -> EXTERNAL_STATE) {
        val currentConsumedState = currentConsumedExternalState
        assertIs<LAST_STATE>(currentConsumedState)
        assertEquals(expectedChange(currentConsumedState), awaitExternalState())
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
    public suspend fun awaitItem(): ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
        return emissions.awaitItem()
    }

    /**
     * Return the next internal state received.
     * This function will suspend if no items have been received.
     *
     * @throws AssertionError if the most recent item was not an internal state.
     */
    public suspend fun awaitInternalState(): INTERNAL_STATE {
        if (settings.awaitState == AwaitState.EXTERNAL_ONLY) {
            error("Internal state not being observed. Override with TestSettings(awaitState = ...)")
        }

        val item = awaitItem()
        return (item as? ItemWithExternalState.InternalStateItem)?.value?.also { currentConsumedInternalState = it }
            ?: fail("Expected Internal State but got $item")
    }

    /**
     * Return the next external state received.
     * This function will suspend if no items have been received.
     *
     * @throws AssertionError if the most recent item was not an external state.
     */
    public suspend fun awaitExternalState(): EXTERNAL_STATE {
        if (settings.awaitState == AwaitState.INTERNAL_ONLY) {
            error("External state not being observed. Override with TestSettings(awaitState = ...)")
        }

        val item = awaitItem()
        return (item as? ItemWithExternalState.ExternalStateItem)?.value?.also { currentConsumedExternalState = it }
            ?: fail("Expected External State but got $item")
    }

    /**
     * Return the next side effect received.
     * This function will suspend if no side effects have been received.
     *
     * @throws AssertionError if the most recent item was not a side effect.
     */
    public suspend fun awaitSideEffect(): SIDE_EFFECT {
        val item = awaitItem()

        return (item as? ItemWithExternalState.SideEffectItem)?.value ?: fail("Expected Side Effect but got $item")
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
