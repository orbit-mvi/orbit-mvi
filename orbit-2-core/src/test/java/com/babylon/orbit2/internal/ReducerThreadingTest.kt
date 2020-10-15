/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2.internal

import com.babylon.orbit2.Container
import com.babylon.orbit2.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random

internal class ReducerThreadingTest {

    @Test
    fun `reductions are applied in order if called from single thread`() {
        // This scenario is meant to simulate calling only reducers from the UI thread
        runBlocking {
            val container = RealContainer<TestState, Nothing>(
                initialState = TestState(),
                parentScope = CoroutineScope(EmptyCoroutineContext),
                settings = Container.Settings()
            )
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
                    else -> throw IllegalStateException("misconfigured test")
                }
            }

            testStateObserver.awaitFor { values.last().ids.size == ITEM_COUNT }

            assertThat(testStateObserver.values.last()).isEqualTo(expectedStates.last())
        }
    }

    @Test
    fun `reductions do not clobber each other when executed from multiple threads`() {
        // This scenario is meant to simulate calling only reducers from the UI thread
        runBlocking {
            val container = RealContainer<TestState, Nothing>(
                initialState = TestState(),
                parentScope = CoroutineScope(EmptyCoroutineContext),
                settings = Container.Settings()
            )
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

                GlobalScope.launch {
                    when (value) {
                        0 -> container.one(true)
                        1 -> container.two(true)
                        2 -> container.three(true)
                        else -> throw IllegalStateException("misconfigured test")
                    }
                }
            }

            testStateObserver.awaitFor { values.last().ids.size == ITEM_COUNT }

            assertThat(testStateObserver.values.last().ids.count { it == 1 }).isEqualTo(ITEM_COUNT / 3)
            assertThat(testStateObserver.values.last().ids.count { it == 2 }).isEqualTo(ITEM_COUNT / 3)
        }
    }

    private data class TestState(val ids: List<Int> = emptyList())

    private val latch = CountDownLatch(ITEM_COUNT)
    private fun Container<TestState, Nothing>.one(delay: Boolean = false) = orbit {
        if (delay) {
            delay(Random.nextLong(20))
        }
        reduce {
            it.copy(ids = state.ids + 1)
        }
        latch.countDown()
    }

    private fun Container<TestState, Nothing>.two(delay: Boolean = false) = orbit {
        if (delay) {
            delay(Random.nextLong(20))
        }
        reduce {
            it.copy(ids = state.ids + 2)
        }
        latch.countDown()
    }

    private fun Container<TestState, Nothing>.three(delay: Boolean = false) = orbit {
        if (delay) {
            delay(Random.nextLong(20))
        }
        reduce {
            it.copy(ids = state.ids + 3)
        }
        latch.countDown()
    }

    private companion object {
        const val ITEM_COUNT = 1119
    }
}
