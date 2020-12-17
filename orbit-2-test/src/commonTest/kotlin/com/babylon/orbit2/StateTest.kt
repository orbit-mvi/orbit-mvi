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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class StateTest {

    @Test
    fun `BLOCKING - succeeds if initial state matches expected state`() {
        ParameterisedStateTest(blocking = true).`succeeds if initial state matches expected state`()
    }

    @Test
    fun `NON BLOCKING - succeeds if initial state matches expected state`() {
        ParameterisedStateTest(blocking = false).`succeeds if initial state matches expected state`()
    }

    @Test
    fun `BLOCKING - fails if initial state does not match expected state`() {
        ParameterisedStateTest(blocking = true).`fails if initial state does not match expected state`()
    }

    @Test
    fun `NON BLOCKING - fails if initial state does not match expected state`() {
        ParameterisedStateTest(blocking = false).`fails if initial state does not match expected state`()
    }

    @Test
    fun `BLOCKING - succeeds if emitted states match expected states`() {
        ParameterisedStateTest(blocking = true).`succeeds if emitted states match expected states`()
    }

    @Test
    fun `NON BLOCKING - succeeds if emitted states match expected states`() {
        ParameterisedStateTest(blocking = false).`succeeds if emitted states match expected states`()
    }

    @Test
    fun `BLOCKING - fails if more states emitted than expected`() {
        ParameterisedStateTest(blocking = true).`fails if more states emitted than expected`()
    }

    @Test
    fun `NON BLOCKING - fails if more states emitted than expected`() {
        ParameterisedStateTest(blocking = false).`fails if more states emitted than expected`()
    }

    @Test
    fun `BLOCKING - fails if one more state expected than emitted`() {
        ParameterisedStateTest(blocking = true).`fails if one more state expected than emitted`()
    }

    @Test
    fun `NON BLOCKING - fails if one more state expected than emitted`() {
        ParameterisedStateTest(blocking = false).`fails if one more state expected than emitted`()
    }

    @Test
    fun `BLOCKING - fails if two more states expected than emitted`() {
        ParameterisedStateTest(blocking = true).`fails if two more states expected than emitted`()
    }

    @Test
    fun `NON BLOCKING - fails if two more states expected than emitted`() {
        ParameterisedStateTest(blocking = false).`fails if two more states expected than emitted`()
    }

    @Test
    fun `BLOCKING - fails if first emitted state does not match expected`() {
        ParameterisedStateTest(blocking = true).`fails if first emitted state does not match expected`()
    }

    @Test
    fun `NON BLOCKING - fails if first emitted state does not match expected`() {
        ParameterisedStateTest(blocking = false).`fails if first emitted state does not match expected`()
    }

    @Test
    fun `BLOCKING - fails if second emitted state does not match expected`() {
        ParameterisedStateTest(blocking = true).`fails if second emitted state does not match expected`()
    }

    @Test
    fun `NON BLOCKING - fails if second emitted state does not match expected`() {
        ParameterisedStateTest(blocking = false).`fails if second emitted state does not match expected`()
    }

    @Test
    fun `BLOCKING - fails if expected states are out of order`() {
        ParameterisedStateTest(blocking = true).`fails if expected states are out of order`()
    }

    @Test
    fun `NON BLOCKING - fails if expected states are out of order`() {
        ParameterisedStateTest(blocking = false).`fails if expected states are out of order`()
    }

    @Test
    fun `BLOCKING - succeeds with dropped assertions`() {
        ParameterisedStateTest(blocking = true).`succeeds with dropped assertions`()
    }

    @Test
    fun `NON BLOCKING - succeeds with dropped assertions`() {
        ParameterisedStateTest(blocking = false).`succeeds with dropped assertions`()
    }

    @Test
    fun `BLOCKING - fails if dropped assertions mean extra states are observed`() {
        ParameterisedStateTest(blocking = true).`fails if dropped assertions mean extra states are observed`()
    }

    @Test
    fun `NON BLOCKING - fails if dropped assertions mean extra states are observed`() {
        ParameterisedStateTest(blocking = false).`fails if dropped assertions mean extra states are observed`()
    }
}
