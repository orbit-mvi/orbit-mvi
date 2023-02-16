/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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

import org.orbitmvi.orbit.ContainerHost

public interface OrbitTestContext<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> {

    /**
     * Invoke `onCreate` lambda for the [ContainerHost].
     */
    public fun runOnCreate()

    /**
     * Invoke an intent on the [ContainerHost] under test.
     */
    public fun invokeIntent(action: CONTAINER_HOST.() -> Unit)

    /**
     * Sanity check assertion. Checks if the initial state is emitted and matches
     * the initial state defined for the production container (or the one specified
     * in the test).
     *
     * @throws AssertionError if initial state does not match.
     */
    public suspend fun expectInitialState()

    /**
     * Return the next item received.
     * This function will suspend if no items have been received.
     */
    public suspend fun awaitItem(): Item<STATE, SIDE_EFFECT>

    /**
     * Return the next state received.
     * This function will suspend if no states have been received.
     *
     * @throws AssertionError if the most recent item was not a state.
     */
    public suspend fun awaitState(): STATE

    /**
     * Return the next side effect received.
     * This function will suspend if no side effects have been received.
     *
     * @throws AssertionError if the most recent item was not a side effect.
     */
    public suspend fun awaitSideEffect(): SIDE_EFFECT

    /**
     * Assert that [count] items were received and ignore them.
     * This function will suspend if no items have been received.
     */
    public suspend fun skipItems(count: Int)

    /**
     * Finish this test and ignore any events which have already been received.
     */
    public suspend fun cancelAndIgnoreRemainingItems()
}
