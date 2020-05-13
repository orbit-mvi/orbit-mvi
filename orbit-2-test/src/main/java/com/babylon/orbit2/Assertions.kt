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

import kotlin.test.assertEquals
import kotlin.test.fail


/**
 * Helper function for asserting orbit state sequences. It applies the reductions specified in `nextState` in a cumulative way, based on
 * successive states.
 *
 * Fails assertions:
 *
 * - When more or less states have been emitted than expected
 * - In ordered mode (default), if an emitted state does not satisfy its corresponding expected assertion-reduction.
 * - In unordered mode, if an emitted state cannot be produced based on the previous using any of the assertion-reductions
 *
 * It is recommended to always use the ordered mode unless we cannot guarantee the order in which the states are emitted.
 *
 * Once an assertion-reduction is satisfied it is removed from further consideration.
 */
internal tailrec fun <T : Any> assertStatesInOrder(
    values: List<T>,
    assertions: List<T.() -> T>,
    previousState: T,
    satisfiedAssertions: Int = 0
) {
    when {
        values.isEmpty() && assertions.isEmpty() -> {
            /* Success! */
        }
        values.isNotEmpty() && assertions.isEmpty() -> failMoreStatesThanExpected(
            assertions,
            satisfiedAssertions,
            values
        )
        assertions.isNotEmpty() -> {
            val assertion = assertions.first()
            val expectedState = previousState.assertion()

            if (expectedState == previousState) {
                // Assertion already satisfied by previous state, drop the assertion and continue the checks in case it was deduplicated by orbit
                println("Expected assertion at index $satisfiedAssertions is satisfied because the object is already in that state")

                assertStatesInOrder(
                    values,
                    assertions.drop(1),
                    previousState,
                    satisfiedAssertions + 1
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
                        satisfiedAssertions + 1
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

private fun <T : Any> failNoStatesReceived(
    assertions: List<T.() -> T>,
    previousState: T
) {
    fail("Expected ${assertions.size} states but none were emitted")
}

private fun <T : Any> failMoreStatesThanExpected(
    assertions: List<T.() -> T>,
    satisfiedAssertions: Int,
    values: List<T>
) {
    // More states received than expected
    fail("Expected ${assertions.size + satisfiedAssertions} states but more were emitted:\n$values")
}