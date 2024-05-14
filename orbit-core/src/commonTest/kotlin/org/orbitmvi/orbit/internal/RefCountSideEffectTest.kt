/*
 * Copyright 2024 Mikołaj Leszczyński & Appmattus Limited
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
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.container
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RefCountSideEffectTest {

    @Test
    fun `side effects are emitted in order`() = runTest {
        val container = backgroundScope.container<Unit, Int>(Unit)

        container.refCountSideEffectFlow.test {
            repeat(1000) {
                container.someFlow(it)
            }

            for (i in 0..999) {
                assertEquals(i, awaitItem())
            }
        }
    }

    @Test
    fun `side effects are cached when there are no subscribers`() = runTest {
        val container = backgroundScope.container<Unit, Int>(Unit)
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)

        container.refCountSideEffectFlow.test {
            assertEquals(action, awaitItem())
            assertEquals(action2, awaitItem())
            assertEquals(action3, awaitItem())
        }
    }

    @Test
    fun `consumed side effects are not resent`() = runTest {
        val container = backgroundScope.container<Unit, Int>(Unit)
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()

        container.refCountSideEffectFlow.test {
            container.someFlow(action)
            container.someFlow(action2)
            container.someFlow(action3)
            assertEquals(action, awaitItem())
            assertEquals(action2, awaitItem())
            assertEquals(action3, awaitItem())
        }

        container.refCountSideEffectFlow.test {
            expectNoEvents()
        }
    }

    @Test
    fun `only new side effects are emitted when resubscribing`() = runTest {
        val container = backgroundScope.container<Unit, Int>(Unit)
        val action = Random.nextInt()

        container.refCountSideEffectFlow.test {
            container.someFlow(action)
            assertEquals(action, awaitItem())
        }

        repeat(1000) {
            container.someFlow(it)
        }

        container.refCountSideEffectFlow.test {
            repeat(1000) {
                assertEquals(it, awaitItem())
            }
        }
    }

    private suspend fun Container<Unit, Int>.someFlow(action: Int) = orbit {
        postSideEffect(action)
    }
}
