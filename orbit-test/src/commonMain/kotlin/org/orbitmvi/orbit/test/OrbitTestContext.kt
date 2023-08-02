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

import kotlinx.coroutines.Job
import org.orbitmvi.orbit.ContainerHost

public interface OrbitTestContext<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> {

    public val containerHost: CONTAINER_HOST

    /**
     * Invoke `onCreate` lambda for the [ContainerHost].
     */
    public fun runOnCreate(): Job

    /**
     * Invoke an intent on the [ContainerHost] under test.
     */
    @Deprecated("Use containerHost instead", ReplaceWith("action(containerHost)"))
    public fun invokeIntent(action: CONTAINER_HOST.() -> Job): Job

    /**
     * Sanity check assertion. Checks if the initial state is emitted and matches
     * the initial state defined for the production container (or the one specified
     * in the test).
     *
     * @throws AssertionError if initial state does not match.
     */
    public suspend fun expectInitialState()

    /**
     * Awaits for a state and checks if it matches the expected state change from the
     * previous state.
     *
     * @param expectedChange The expected change from the previous state.
     *
     * @throws AssertionError if state does not match the expected change.
     */
    public suspend fun expectState(expectedChange: STATE.() -> STATE)

    /**
     * Awaits for a state and checks if it matches the expected state.
     *
     * @param expected The expected state.
     *
     * @throws AssertionError if state does not match the expected state.
     */
    public suspend fun expectState(expected: STATE)

    /**
     * Awaits for a side effect and checks if it matches the expected side effect
     *
     * @param expected The expected side effect
     *
     * @throws AssertionError if state does not match the expected change.
     */
    public suspend fun expectSideEffect(expected: SIDE_EFFECT)

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
     * This also cancels any in-progress intents.
     */
    public suspend fun cancelAndIgnoreRemainingItems()
}
