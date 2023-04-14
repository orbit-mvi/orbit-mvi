/*
 * Copyright 2021-2023 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test
import org.orbitmvi.orbit.test.runBlocking
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class ContainerThreadingTest {

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun `container can process a second action while the first is suspended`() {
        val container = scope.container<Int, Nothing>(Random.nextInt())
        val observer = container.stateFlow.test()
        val newState = Random.nextInt()

        runBlocking {
            container.orbit {
                delay(Long.MAX_VALUE)
            }
            container.orbit {
                reduce(null) { newState }
            }
        }

        observer.awaitCount(2)
        assertEquals(newState, container.stateFlow.value)
    }

    @Test
    fun `reductions are applied in order if called from single thread`() {
        // This scenario is meant to simulate calling only reducers from the UI thread
        runBlocking {
            val container = scope.container<TestState, Nothing>(TestState())
            val testStateObserver = container.stateFlow.test()
            val expectedStates = mutableListOf(
                TestState(
                    emptyList()
                )
            )
            for (i in 0 until ITEM_COUNT) {
                val value = (i % 3)
                expectedStates.add(
                    expectedStates.last().copy(ids = expectedStates.last().ids + (value + 1))
                )

                when (value) {
                    0 -> container.one()
                    1 -> container.two()
                    2 -> container.three()
                    else -> error("misconfigured test")
                }
            }

            testStateObserver.awaitFor { values.isNotEmpty() && values.last().ids.size == ITEM_COUNT }

            assertEquals(expectedStates.last(), testStateObserver.values.last())
        }
    }

    @Test
    fun `reductions run in sequence but in an undefined order when executed from multiple threads`() {
        // This scenario is meant to simulate calling only reducers from the UI thread
        runBlocking {
            val container = scope.container<TestState, Nothing>(TestState())
            val testStateObserver = container.stateFlow.test()
            val expectedStates = mutableListOf(
                TestState(
                    emptyList()
                )
            )
            coroutineScope {
                for (i in 0 until ITEM_COUNT) {
                    val value = (i % 3)
                    expectedStates.add(
                        expectedStates.last().copy(ids = expectedStates.last().ids + (value + 1))
                    )

                    launch {
                        when (value) {
                            0 -> container.one(true)
                            1 -> container.two(true)
                            2 -> container.three(true)
                            else -> error("misconfigured test")
                        }
                    }
                }
            }

            testStateObserver.awaitFor { values.isNotEmpty() && values.last().ids.size == ITEM_COUNT }

            assertEquals(ITEM_COUNT / 3, testStateObserver.values.last().ids.count { it == 1 })
            assertEquals(ITEM_COUNT / 3, testStateObserver.values.last().ids.count { it == 2 })
        }
    }

    private data class TestState(val ids: List<Int> = emptyList())

    private suspend fun Container<TestState, Nothing>.one(delay: Boolean = false) = orbit {
        if (delay) {
            delay(Random.nextLong(20))
        }
        reduce(null) {
            it.copy(ids = state.ids + 1)
        }
    }

    private suspend fun Container<TestState, Nothing>.two(delay: Boolean = false) = orbit {
        if (delay) {
            delay(Random.nextLong(20))
        }
        reduce(null) {
            it.copy(ids = state.ids + 2)
        }
    }

    private suspend fun Container<TestState, Nothing>.three(delay: Boolean = false) = orbit {
        if (delay) {
            delay(Random.nextLong(20))
        }
        reduce(null) {
            it.copy(ids = state.ids + 3)
        }
    }

    private companion object {
        const val ITEM_COUNT = 1119
    }
}
