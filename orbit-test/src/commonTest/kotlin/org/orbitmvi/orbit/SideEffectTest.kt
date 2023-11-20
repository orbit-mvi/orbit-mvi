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
internal class SideEffectTest {
    private lateinit var testCase: ParameterisedSideEffectTest

    @Test
    fun BLOCKING___succeedsif_posted_side_effects_match_expected_side_effects() {
        testCase = ParameterisedSideEffectTest(blocking = true)
        testCase.succeeds_if_posted_side_effects_match_expected_side_effects()
    }

    @Test
    fun NON_BLOCKING___succeeds_if_posted_side_effects_match_expected_side_effects() {
        testCase = ParameterisedSideEffectTest(blocking = false)
        testCase.succeeds_if_posted_side_effects_match_expected_side_effects()
    }

    @Test
    fun BLOCKING___fails_if_posted_side_effects_do_not_match_expected_side_effects() {
        testCase = ParameterisedSideEffectTest(blocking = true)
        testCase.fails_if_posted_side_effects_do_not_match_expected_side_effects()
    }

    @Test
    fun NON_BLOCKING___failsif_posted_side_effects_do_not_match_expected_side_effects() {
        testCase = ParameterisedSideEffectTest(blocking = false)
        testCase.fails_if_posted_side_effects_do_not_match_expected_side_effects()
    }
}
