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
    fun `BLOCKING - succeeds if initial state matches expected state`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`succeeds if initial state matches expected state`()
    }

    @Test
    fun `NON BLOCKING - succeeds if initial state matches expected state`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`succeeds if initial state matches expected state`()
    }

    @Test
    fun `BLOCKING - fails if initial state does not match expected state`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`fails if initial state does not match expected state`()
    }

    @Test
    fun `NON BLOCKING - fails if initial state does not match expected state`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`fails if initial state does not match expected state`()
    }

    @Test
    fun `BLOCKING - succeeds if emitted states match expected states`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`succeeds if emitted states match expected states`()
    }

    @Test
    fun `NON BLOCKING - succeeds if emitted states match expected states`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`succeeds if emitted states match expected states`()
    }

    @Test
    fun `BLOCKING - fails if more states emitted than expected`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`fails if more states emitted than expected`()
    }

    @Test
    fun `NON BLOCKING - fails if more states emitted than expected`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`fails if more states emitted than expected`()
    }

    @Test
    fun `BLOCKING - fails if one more state expected than emitted`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`fails if one more state expected than emitted`()
    }

    @Test
    fun `NON BLOCKING - fails if one more state expected than emitted`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`fails if one more state expected than emitted`()
    }

    @Test
    fun `BLOCKING - fails if two more states expected than emitted`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`fails if two more states expected than emitted`()
    }

    @Test
    fun `NON BLOCKING - fails if two more states expected than emitted`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`fails if two more states expected than emitted`()
    }

    @Test
    fun `BLOCKING - fails if first emitted state does not match expected`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`fails if first emitted state does not match expected`()
    }

    @Test
    fun `NON BLOCKING - fails if first emitted state does not match expected`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`fails if first emitted state does not match expected`()
    }

    @Test
    fun `BLOCKING - fails if second emitted state does not match expected`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`fails if second emitted state does not match expected`()
    }

    @Test
    fun `NON BLOCKING - fails if second emitted state does not match expected`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`fails if second emitted state does not match expected`()
    }

    @Test
    fun `BLOCKING - fails if expected states are out of order`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`fails if expected states are out of order`()
    }

    @Test
    fun `NON BLOCKING - fails if expected states are out of order`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`fails if expected states are out of order`()
    }

    @Test
    fun `BLOCKING - succeeds with dropped assertions`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`succeeds with dropped assertions`()
    }

    @Test
    fun `NON BLOCKING - succeeds with dropped assertions`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`succeeds with dropped assertions`()
    }

    @Test
    fun `BLOCKING - fails if dropped assertions mean extra states are observed`() {
        testCase = ParameterisedStateTest(blocking = true)
        testCase.`fails if dropped assertions mean extra states are observed`()
    }

    @Test
    fun `NON BLOCKING - fails if dropped assertions mean extra states are observed`() {
        testCase = ParameterisedStateTest(blocking = false)
        testCase.`fails if dropped assertions mean extra states are observed`()
    }
}
