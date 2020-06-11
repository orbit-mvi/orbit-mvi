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

package com.babylon.orbit2

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ReducerOrderingTest {

    @Test
    fun `reductions are applied in sequence`() {
        runBlocking {
            val middleware = ThreeReducersMiddleware()
            val testStateObserver = middleware.container.stateStream.test()
            val expectedStates = mutableListOf(
                TestState(
                    emptyList()
                )
            )

            for (i in 0 until 1119) {
                val value = (i % 3)
                expectedStates.add(
                    expectedStates.last().copy(ids = expectedStates.last().ids + (value + 1))
                )

                when (value) {
                    0 -> middleware.one()
                    1 -> middleware.two()
                    2 -> middleware.three()
                    else -> throw IllegalStateException("misconfigured test")
                }
            }

            testStateObserver.awaitCount(1120)

            assertThat(testStateObserver.values).containsExactlyElementsOf(expectedStates)
        }
    }

    private data class TestState(val ids: List<Int> = emptyList())

    private class ThreeReducersMiddleware : Host<TestState, String> {
        override val container = Container.create<TestState, String>(
            TestState()
        )

        fun one() = orbit {
            reduce {
                state.copy(ids = state.ids + 1)
            }
        }

        fun two() = orbit {
            reduce {
                state.copy(ids = state.ids + 2)
            }
        }

        fun three() = orbit {
            reduce {
                state.copy(ids = state.ids + 3)
            }
        }
    }
}
