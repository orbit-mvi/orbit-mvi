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
    @Test
    fun `BLOCKING - succeeds if posted side effects match expected side effects`() {
        ParameterisedSideEffectTest(blocking = true)
            .`succeeds if posted side effects match expected side effects`()
    }

    @Test
    fun `NON BLOCKING - succeeds if posted side effects match expected side effects`() {
        ParameterisedSideEffectTest(blocking = false)
            .`succeeds if posted side effects match expected side effects`()
    }

    @Test
    fun `BLOCKING - fails if posted side effects do not match expected side effects`() {
        ParameterisedSideEffectTest(blocking = true)
            .`fails if posted side effects do not match expected side effects`()
    }

    @Test
    fun `NON BLOCKING - fails if posted side effects do not match expected side effects`() {
        ParameterisedSideEffectTest(blocking = false)
            .`fails if posted side effects do not match expected side effects`()
    }
}
