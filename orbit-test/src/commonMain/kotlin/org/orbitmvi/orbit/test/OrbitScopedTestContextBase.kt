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
import kotlin.test.fail

public abstract class OrbitScopedTestContextBase<
        INTERNAL_STATE : Any,
        EXTERNAL_STATE : Any,
        SIDE_EFFECT : Any,
        CONTAINER_HOST : ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>
        >(
    public val containerHost: CONTAINER_HOST,
    private val emissions: ReceiveTurbine<ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>>,
) {

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
