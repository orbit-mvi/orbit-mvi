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

import app.cash.turbine.ReceiveTurbine
import kotlinx.coroutines.Job
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitInternal
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.coroutines.runBlocking

public class RealOrbitTestContext<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    override val containerHost: CONTAINER_HOST,
    initialState: STATE?,
    private val emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>>
) : OrbitTestContext<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    private val resolvedInitialState: STATE by lazy {
        initialState ?: containerHost.container.findTestContainer().originalInitialState
    }
    private var currentConsumedState: STATE = resolvedInitialState

    @OptIn(OrbitInternal::class)
    override fun runOnCreate(): Job {
        val onCreate = containerHost.container.findOnCreate()
        return runBlocking {
            containerHost.container.orbit(onCreate)
        }
    }

    @Deprecated("Use containerHost instead", replaceWith = ReplaceWith("action(containerHost)"))
    override fun invokeIntent(action: CONTAINER_HOST.() -> Job): Job {
        return containerHost.action()
    }

    override suspend fun awaitItem(): Item<STATE, SIDE_EFFECT> {
        return emissions.awaitItem()
    }

    public override suspend fun awaitState(): STATE {
        val item = awaitItem()

        return (item as? Item.StateItem)?.value?.also { currentConsumedState = it } ?: fail("Expected State but got $item")
    }

    public override suspend fun awaitSideEffect(): SIDE_EFFECT {
        val item = awaitItem()

        return (item as? Item.SideEffectItem)?.value ?: fail("Expected Side Effect but got $item")
    }

    override suspend fun cancelAndIgnoreRemainingItems() {
        emissions.cancelAndIgnoreRemainingEvents()
        containerHost.container.cancel()
    }

    override suspend fun expectInitialState() {
        assertEquals(resolvedInitialState, awaitState())
    }

    override suspend fun expectState(expected: STATE) {
        assertEquals(expected, awaitState())
    }

    override suspend fun expectSideEffect(expected: SIDE_EFFECT) {
        assertEquals(expected, awaitSideEffect())
    }

    override suspend fun expectNoItems() {
        emissions.expectNoEvents()
    }

    override suspend fun expectState(expectedChange: STATE.() -> STATE) {
        assertEquals(expectedChange(currentConsumedState), awaitState())
    }

    override suspend fun skipItems(count: Int) {
        emissions.skipItems(count)
    }
}
