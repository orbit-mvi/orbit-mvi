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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class StateTest {
    private lateinit var testCase: ParameterisedStateTest

    @Test
    fun BLOCKING___succeedsif_initial_state_matches_expected_state() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.succeeds_if_initial_state_matches_expected_state()
    }

    @Test
    fun NON_BLOCKING___succeeds_if_initial_state_matches_expected_state() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.succeeds_if_initial_state_matches_expected_state()
    }

    @Test
    fun BLOCKING_fails_if_initial_state_does_not_match_expected_state() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.fails_if_initial_state_does_not_match_expected_state()
    }

    @Test
    fun NON_BLOCKING___fails_if_initial_state_does_not_match_expected_state() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.fails_if_initial_state_does_not_match_expected_state()
    }

    @Test
    fun BLOCKING___succeeds_if_emitted_states_match_expected_states() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.succeeds_if_emitted_states_match_expected_states()
    }

    @Test
    fun NON_BLOCKING___succeeds_if_emitted_states_match_expected_states() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.succeeds_if_emitted_states_match_expected_states()
    }

    @Test
    fun BLOCKING___fails_if_more_states_emitted_than_expected() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.fails_if_more_states_emitted_than_expected()
    }

    @Test
    fun NON_BLOCKING___fails_if_more_states_emitted_than_expected() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.fails_if_more_states_emitted_than_expected()
    }

    @Test
    fun BLOCKING___fails_if_one_more_state_expected_than_emitted() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.fails_if_one_more_state_expected_than_emitted()
    }

    @Test
    fun NON_BLOCKING___fails_if_one_more_state_expected_than_emitted() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.fails_if_one_more_state_expected_than_emitted()
    }

    @Test
    fun BLOCKING___fails_if_two_more_states_expected_than_emitted() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.fails_if_two_more_states_expected_than_emitted()
    }

    @Test
    fun NON_BLOCKING___fails_if_two_more_states_expected_than_emitted() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.fails_if_two_more_states_expected_than_emitted()
    }

    @Test
    fun BLOCKING___fails_if_first_emitted_state_does_not_match_expected() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.fails_if_first_emitted_state_does_not_match_expected()
    }

    @Test
    fun NON_BLOCKING___fails_if_first_emitted_state_does_not_match_expected() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.fails_if_first_emitted_state_does_not_match_expected()
    }

    @Test
    fun BLOCKING___fails_if_second_emitted_state_does_not_match_expected() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.fails_if_second_emitted_state_does_not_match_expected()
    }

    @Test
    fun NON_BLOCKING___fails_if_second_emitted_state_does_not_match_expected() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.fails_if_second_emitted_state_does_not_match_expected()
    }

    @Test
    fun BLOCKING___fails_if_expected_states_are_out_of_order() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.fails_if_expected_states_are_out_of_order()
    }

    @Test
    fun NON_BLOCKING___fails_if_expected_states_are_out_of_order() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.fails_if_expected_states_are_out_of_order()
    }

    @Test
    fun BLOCKING___succeeds_with_dropped_assertions() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.succeeds_with_dropped_assertions()
    }

    @Test
    fun NON_BLOCKING___succeeds_with_dropped_assertions() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.succeeds_with_dropped_assertions()
    }

    @Test
    fun BLOCKING___fails_if_dropped_assertions_mean_extra_states_are_observed() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.fails_if_dropped_assertions_mean_extra_states_are_observed()
    }

    @Test
    fun NON_BLOCKING___fails_if_dropped_assertions_mean_extra_states_are_observed() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.fails_if_dropped_assertions_mean_extra_states_are_observed()
    }
}
