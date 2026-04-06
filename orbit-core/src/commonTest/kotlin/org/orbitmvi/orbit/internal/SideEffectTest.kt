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
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.orbitContainer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class SideEffectTest {

    @Test
    fun side_effects_are_emitted_in_order() = runTest {
        val container = backgroundScope.orbitContainer<Unit, Int>(Unit)

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
    fun side_effects_are_cached_when_there_are_no_subscribers() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = backgroundScope.orbitContainer<Unit, Int>(Unit)

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
    fun consumed_side_effects_are_not_resent() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = backgroundScope.orbitContainer<Unit, Int>(Unit)

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
    }

    @Test
    fun collecting_side_effect_flow_more_than_once_throws() = runTest {
        val container = backgroundScope.orbitContainer<Unit, Int>(Unit)

        container.sideEffectFlow.test {
            container.someFlow(1)
            assertEquals(1, awaitItem())
            cancel()
        }

        container.sideEffectFlow.test {
            val error = awaitError()
            assertIs<IllegalStateException>(error)
        }
    }

    private suspend fun OrbitContainer<Unit, Unit, Int>.someFlow(action: Int) = orbit {
        postSideEffect(action)
    }
}
