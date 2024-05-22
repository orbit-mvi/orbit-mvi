/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.internal

import app.cash.turbine.test
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.container
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SideEffectTest {

    @Test
    fun `side effects are emitted in order`() = runTest {
        val container = backgroundScope.container<Unit, Int>(Unit)

        container.sideEffectFlow.test {
            repeat(1000) {
                container.someFlow(it)
            }

            repeat(1000) {
                assertEquals(it, awaitItem())
            }
        }
    }

    @Test
    fun `side effects are cached when there are no subscribers`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = backgroundScope.container<Unit, Int>(Unit)

        joinAll(
            container.someFlow(action),
            container.someFlow(action2),
            container.someFlow(action3)
        )

        container.sideEffectFlow.test {
            assertEquals(action, awaitItem())
            assertEquals(action2, awaitItem())
            assertEquals(action3, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `consumed side effects are not resent`() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = backgroundScope.container<Unit, Int>(Unit)

        joinAll(
            container.someFlow(action),
            container.someFlow(action2),
            container.someFlow(action3)
        )

        container.sideEffectFlow.test {
            assertEquals(action, awaitItem())
            assertEquals(action2, awaitItem())
            assertEquals(action3, awaitItem())
            ensureAllEventsConsumed()
            cancel()
        }

        container.sideEffectFlow.test {
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun `only new side effects are emitted when resubscribing`() = runTest {
        val action = Random.nextInt()
        val container = backgroundScope.container<Unit, Int>(Unit)

        container.sideEffectFlow.test {
            container.someFlow(action)
            skipItems(1)
            ensureAllEventsConsumed()
            cancel()
        }

        launch {
            repeat(100) {
                container.someFlow(it)
            }
        }

        container.sideEffectFlow.test {
            repeat(100) {
                assertEquals(it, awaitItem())
            }
            ensureAllEventsConsumed()
            cancel()
        }
    }

    private suspend fun Container<Unit, Int>.someFlow(action: Int) = orbit {
        postSideEffect(action)
    }
}
