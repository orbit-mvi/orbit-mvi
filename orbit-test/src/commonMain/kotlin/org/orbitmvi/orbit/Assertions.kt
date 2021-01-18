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

package org.orbitmvi.orbit

import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Helper function for asserting orbit state sequences. It applies the reductions specified in
 * `nextState` in a cumulative way, based on successive states.
 *
 * Fails assertions:
 *
 * - When more or less states have been emitted than expected
 * - If an emitted state does not satisfy its corresponding expected assertions.
 *
 * Once an assertion is satisfied it is removed from further consideration.
 */
internal tailrec fun <T : Any> assertStatesInOrder(
    values: List<T>,
    assertions: List<T.() -> T>,
    previousState: T,
    satisfiedAssertions: Int = 0,
    droppedAssertions: Int = 0
) {
    when {
        values.isEmpty() && assertions.isEmpty() -> {
            /* Success! */
        }
        values.isNotEmpty() && assertions.isEmpty() -> failMoreStatesThanExpected(
            assertions,
            satisfiedAssertions,
            droppedAssertions,
            values
        )
        assertions.isNotEmpty() -> {
            val assertion = assertions.first()
            val expectedState = previousState.assertion()

            if (expectedState == previousState) {
                // Assertion already satisfied by previous state, drop the assertion and continue the checks in case it was deduplicated by orbit
                println("Assertion at index $satisfiedAssertions is satisfied because the object is already in that state")

                assertStatesInOrder(
                    values,
                    assertions.drop(1),
                    previousState,
                    satisfiedAssertions + 1,
                    droppedAssertions + 1
                )
            } else {
                val actualState = values.firstOrNull()
                if (actualState == null) {
                    failLessStatesReceivedThanExpected(
                        assertions,
                        previousState,
                        satisfiedAssertions
                    )
                } else {
                    assertEquals(
                        expectedState,
                        actualState,
                        "Failed assertion at index $satisfiedAssertions"
                    )

                    assertStatesInOrder(
                        values.drop(1),
                        assertions.drop(1),
                        actualState,
                        satisfiedAssertions + 1,
                        droppedAssertions
                    )
                }
            }
        }
    }
}

private fun <T : Any> failLessStatesReceivedThanExpected(
    assertions: List<T.() -> T>,
    previousState: T,
    satisfiedAssertions: Int
) {
    val expectedStates =
        assertions
            .fold(emptyList<T>()) { list, reducer ->
                list + (
                        list.lastOrNull() ?: previousState
                        ).reducer()
            }
    fail(
        "Failed assertions at indices ${satisfiedAssertions until (satisfiedAssertions + assertions.size)}, " +
                "expected states but never received:\n$expectedStates"
    )
}

private fun <T : Any> failMoreStatesThanExpected(
    assertions: List<T.() -> T>,
    satisfiedAssertions: Int,
    droppedAssertions: Int,
    values: List<T>
) {
    if (droppedAssertions == 0) {
        // More states received than expected
        fail(
            "Expected ${assertions.size + satisfiedAssertions} states" +
                " but more were emitted:\n$values"
        )
    } else {
        // More states received than expected, but some assertions were dropped
        fail(
            "Expected ${assertions.size + satisfiedAssertions} states" +
                " but more were emitted:\n$values\n\n" +
                    "Caution: $droppedAssertions assertions were dropped as they encountered a " +
                    "current state which already satisfied them."
        )
    }
}
