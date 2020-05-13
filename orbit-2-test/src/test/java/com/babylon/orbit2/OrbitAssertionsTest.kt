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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrbitAssertionsTest {
    @Test
    fun `FAIL - No states emitted`() {
        // Given Empty state list
        val stateList: List<TestState> = emptyList()
        // And Assertion list with some assertions
        val assertions = listOf<TestState.() -> TestState>(
            { copy(label = label + "bar") },
            { copy(label = label + "baz") },
            { copy(index = 2) }
        )

        // When I assert the state list
        val throwable = assertThrows<AssertionError> {
            assertStatesInOrder(stateList, assertions, TestState())
        }

        // Then The failure message points us to the failed assertion
        assertThat(throwable)
            .hasMessage(
                "Failed assertions at indices 0..2, expected states but never received:\n" +
                        "[" +
                        "TestState(label=foobar, index=1), " +
                        "TestState(label=foobarbaz, index=1), " +
                        "TestState(label=foobarbaz, index=2)" +
                        "]"
            )
    }


    @Test
    fun `PASS - No states, no assertions`() {
        // Given Empty state list"
        val stateList: List<TestState> = emptyList()

        // And An empty assertion list"
        val assertions: List<TestState.() -> TestState> = emptyList()

        // Then The assertion passes
        assertStatesInOrder(stateList, assertions, TestState())
    }

    @Test
    fun `PASS - Several states in order, matching assertions`() {

        // Given State list with several states
        val stateList: List<TestState> = listOf(
            TestState("foobar", 1),
            TestState("foobarbaz", 1),
            TestState("foobarbaz", 2)
        )

        // And Assertion list with matching assertions
        val assertions = listOf<TestState.() -> TestState>(
            { copy(label = label + "bar") },
            { copy(label = label + "baz") },
            { copy(index = 2) }
        )

        // Then The assertion passes
        assertStatesInOrder(stateList, assertions, TestState())
    }

    @Test
    fun `PASS - states with already satisfied assertion`() {

        // Given State list with several states
        val stateList: List<TestState> = listOf(
            TestState("foobar", 2),
            TestState("foobarbaz", 2),
            TestState("foobarbaz", 3)
        )

        // And Assertion list with matching assertions
        val assertions = listOf<TestState.() -> TestState>(
            { copy(index = 2) },
            { copy(label = label + "bar") },
            { copy(label = label + "baz") },
            { copy(index = 3) }
        )

        // Then The assertion passes
        assertStatesInOrder(stateList, assertions, TestState(index = 2))
    }

    @Test
    fun `PASS - states with already satisfied assertions the end`() {

        // Given State list with several states
        val stateList: List<TestState> = listOf()

        // And Assertion list with matching assertions
        val assertions = listOf<TestState.() -> TestState>(
            { copy(index = 2) },
            { copy(index = 2) },
            { copy(index = 2) }
        )

        // Then The assertion passes
        assertStatesInOrder(stateList, assertions, TestState(index = 2))
    }

    @Test
    fun `FAIL - More states emitted than expected`() {

        // Given State list with several states
        val stateList: List<TestState> = listOf(
            TestState("foobar", 1),
            TestState("foobarbaz", 1),
            TestState("foobarbaz", 2),
            TestState("foobarbaz", 4),
            TestState("foobarbaz", 6)
        )
        // And Assertion list with less matching assertions than states
        val assertions = listOf<TestState.() -> TestState>(
            { copy(label = label + "bar") },
            { copy(label = label + "baz") },
            { copy(index = 2) }
        )

        // When I assert the state list
        val throwable = assertThrows<AssertionError> {
            assertStatesInOrder(stateList, assertions, TestState())
        }

        // Then The failure message points us to the failed assertion
        assertThat(throwable)
            .hasMessage(
                "Expected 3 states but more were emitted:\n" +
                        "[TestState(label=foobarbaz, index=4), TestState(label=foobarbaz, index=6)]"
            )
    }

    @Test
    fun `FAIL - Several states out of order vs matching assertions`() {
        // Given State list with several states
        val stateList: List<TestState> = listOf(
            TestState("foobar", 1),
            TestState("foobarbaz", 1),
            TestState("foobarbaz", 2)
        )
        // And Assertion list with matching assertions in a different order
        val assertions = listOf<TestState.() -> TestState>(
            { copy(label = label + "baz") },
            { copy(label = label + "bar") },
            { copy(index = 3) }
        )

        // When I assert the state list
        val throwable = assertThrows<AssertionError> {
            assertStatesInOrder(stateList, assertions, TestState())
        }

        // Then The failure message points us to the failed assertion
        assertThat(throwable)
            .hasMessage(
                "Failed assertion at index 0. Expected <TestState(label=foobaz, index=1)>, " +
                        "actual <TestState(label=foobar, index=1)>."
            )
    }

    @Test
    fun `FAIL - Permuted states out of order vs assertions`() {

        listOf(
            TestState("foobar", 1),
            TestState("foobarbaz", 1),
            TestState("foobarbaz", 2)
        )
            .permutations()
            .drop(1) // filter away the successful case
            .forEach {
                // Given State list with several states
                val stateList: List<TestState> = it
                // And Assertion list with matching assertions in a set order
                val assertions = listOf<TestState.() -> TestState>(
                    { copy(label = label + "bar") },
                    { copy(label = label + "baz") },
                    { copy(index = 2) }
                )

                // Then The assertion fails
                assertThrows<AssertionError> {
                    assertStatesInOrder(stateList, assertions, TestState())
                }
            }
    }

    @Test
    fun `FAIL - Permuted assertions out of order vs states`() {

        listOf<TestState.() -> TestState>(
            { copy(label = label + "bar") },
            { copy(label = label + "baz") },
            { copy(index = 2) }
        )
            .permutations()
            .drop(1) // filter away the successful case
            .forEach {

                // Given State list with several states
                val stateList: List<TestState> = listOf(
                    TestState("foobar", 1),
                    TestState("foobarbaz", 1),
                    TestState("foobarbaz", 2)
                )
                // And Assertion list with matching assertions in a set order
                val assertions: List<TestState.() -> TestState> = it

                // Then The assertion fails
                assertThrows<AssertionError> {
                    assertStatesInOrder(stateList, assertions, TestState())
                }
            }
    }
}

private data class TestState(
    val label: String = "foo",
    val index: Int = 1
)

private inline fun <reified T> List<T>.permutations(): Sequence<List<T>> = sequence {
    heapPermutation(toTypedArray(), size)
}

// Generating permutation using Heap Algorithm
private suspend fun <T> SequenceScope<List<T>>.heapPermutation(
    a: Array<T>,
    size: Int
) {
    // if size becomes 1 then prints the obtained
    // permutation
    if (size == 1) yield(listOf(*a))

    (0 until size).forEach {
        heapPermutation(a, size - 1)

        // if size is odd, swap first and last
        // element
        if (size % 2 == 1) {
            val temp = a[0]
            a[0] = a[size - 1]
            a[size - 1] = temp
        }

        // If size is even, swap ith and last
        // element
        else {
            val temp = a[it]
            a[it] = a[size - 1]
            a[size - 1] = temp
        }
    }
}
