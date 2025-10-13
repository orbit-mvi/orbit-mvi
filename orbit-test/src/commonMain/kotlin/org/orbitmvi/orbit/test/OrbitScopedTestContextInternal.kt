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
import org.orbitmvi.orbit.ContainerHostWithExternalState
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

public class OrbitScopedTestContextInternal<
        INTERNAL_STATE : Any,
        EXTERNAL_STATE : Any,
        SIDE_EFFECT : Any,
        CONTAINER_HOST : ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>
        >(
    containerHost: CONTAINER_HOST,
    resolvedInitialState: INTERNAL_STATE,
    emissions: ReceiveTurbine<ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>>,
) : OrbitScopedTestContextBase<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT, CONTAINER_HOST>(containerHost, emissions) {
    @PublishedApi
    internal var currentConsumedInternalState: INTERNAL_STATE = resolvedInitialState

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
     * Return the next internal state received.
     * This function will suspend if no items have been received.
     *
     * @throws AssertionError if the most recent item was not an internal state.
     */
    public suspend fun awaitInternalState(): INTERNAL_STATE {
        val item = awaitItem()
        return (item as? ItemWithExternalState.InternalStateItem)?.value?.also { currentConsumedInternalState = it }
            ?: fail("Expected Internal State but got $item")
    }
}
