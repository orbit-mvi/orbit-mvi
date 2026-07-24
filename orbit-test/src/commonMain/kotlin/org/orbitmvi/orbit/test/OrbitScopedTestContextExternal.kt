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
import org.orbitmvi.orbit.OrbitContainerHost
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

public class OrbitScopedTestContextExternal<
    INTERNAL_STATE : Any,
    EXTERNAL_STATE : Any,
    SIDE_EFFECT : Any,
    CONTAINER_HOST : OrbitContainerHost<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>
    >(
    containerHost: CONTAINER_HOST,
    resolvedInitialState: INTERNAL_STATE,
    emissions: ReceiveTurbine<ItemWithInternalAndExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>>,
) : OrbitScopedTestContextBase<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT, CONTAINER_HOST>(containerHost, emissions) {

    @PublishedApi
    internal var currentConsumedExternalState: EXTERNAL_STATE = containerHost.container.findTransformState()(
        resolvedInitialState
    )

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
     * Return the next external state received.
     * This function will suspend if no items have been received.
     *
     * @throws AssertionError if the most recent item was not an external state.
     */
    public suspend fun awaitExternalState(): EXTERNAL_STATE {
        val item = awaitRawItem()
        return (item as? ItemWithInternalAndExternalState.ExternalStateItem)?.value?.also { currentConsumedExternalState = it }
            ?: fail("Expected External State but got $item")
    }

    /**
     * Return the next item received as either an external state or a side effect.
     * This function will suspend if no items have been received.
     *
     * Useful when the ordering of states and side effects is not deterministic and you need to
     * drain items until a particular one arrives. Consuming an external state advances the state
     * used for subsequent relative assertions (e.g. [expectExternalState]).
     */
    public suspend fun awaitItem(): Item<EXTERNAL_STATE, SIDE_EFFECT> {
        return when (val item = awaitRawItem()) {
            is ItemWithInternalAndExternalState.ExternalStateItem ->
                Item.StateItem(item.value.also { currentConsumedExternalState = it })

            is ItemWithInternalAndExternalState.SideEffectItem -> Item.SideEffectItem(item.value)

            is ItemWithInternalAndExternalState.InternalStateItem ->
                fail("Expected an external state or side effect but got $item")
        }
    }
}
