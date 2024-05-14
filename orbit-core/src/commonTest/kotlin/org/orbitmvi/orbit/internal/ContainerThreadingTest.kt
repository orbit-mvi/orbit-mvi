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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.container
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ContainerThreadingTest {

    @Test
    fun `container can process a second action while the first is suspended`() = runTest {
        val initState = Random.nextInt()
        val newState = Random.nextInt()
        val container = backgroundScope.container<Int, Nothing>(initState)

        container.stateFlow.test {
            container.orbit {
                delay(Long.MAX_VALUE)
            }
            container.orbit {
                reduce { newState }
            }
            assertEquals(initState, awaitItem())
            assertEquals(newState, awaitItem())
        }
    }

    @Test
    fun `reductions are applied in order if called from single thread`() = runTest {
        var expectedState = TestState()
        val container = backgroundScope.container<TestState, Nothing>(expectedState)
        container.stateFlow.test {
            skipItems(1)

            for (i in 0 until ITEM_COUNT) {
                val value = (i % 3)
                expectedState = expectedState.copy(ids = expectedState.ids + (value + 1))

                when (value) {
                    0 -> container.one()
                    1 -> container.two()
                    2 -> container.three()
                    else -> error("misconfigured test")
                }

                assertEquals(expectedState, awaitItem())
            }
        }
    }

    @Test
    fun `reductions run in sequence but in an undefined order when executed from multiple threads`() = runTest {
        val expectedStates = mutableListOf(
            TestState(
                emptyList()
            )
        )
        val container = backgroundScope.container<TestState, Nothing>(expectedStates.first())
        container.stateFlow.test {
            skipItems(1)

            for (i in 0 until ITEM_COUNT) {
                val value = (i % 3)
                expectedStates.add(
                    expectedStates.last().copy(ids = expectedStates.last().ids + (value + 1))
                )

                launch {
                    when (value) {
                        0 -> container.one()
                        1 -> container.two()
                        2 -> container.three()
                        else -> error("misconfigured test")
                    }
                }
            }

            skipItems(ITEM_COUNT - 1)
            val lastState = awaitItem()
            assertEquals(ITEM_COUNT / 3, lastState.ids.count { it == 1 })
            assertEquals(ITEM_COUNT / 3, lastState.ids.count { it == 2 })
        }
    }

    private data class TestState(val ids: List<Int> = emptyList())

    private suspend fun Container<TestState, Nothing>.one(delay: Boolean = false) = orbit {
        if (delay) {
            delay(Random.nextLong(20))
        }
        reduce {
            it.copy(ids = state.ids + 1)
        }
    }

    private suspend fun Container<TestState, Nothing>.two(delay: Boolean = false) = orbit {
        if (delay) {
            delay(Random.nextLong(20))
        }
        reduce {
            it.copy(ids = state.ids + 2)
        }
    }

    private suspend fun Container<TestState, Nothing>.three(delay: Boolean = false) = orbit {
        if (delay) {
            delay(Random.nextLong(20))
        }
        reduce {
            it.copy(ids = state.ids + 3)
        }
    }

    private companion object {
        const val ITEM_COUNT = 1119
    }
}
