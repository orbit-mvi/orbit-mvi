/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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
 */

package org.orbitmvi.orbit

import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.internal.ExternalStateContainerAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Suppress("DEPRECATION")
@OptIn(OrbitInternal::class)
class ContainerWithExternalStateTest {

    @Test
    fun `with external state exposes the transform on the adapter`() = runTest {
        val container = backgroundScope.orbitContainer<Int, Int>(initialState = 1)
            .withExternalState { it.toString() }

        val adapter = assertIs<ExternalStateContainerAdapter<Int, String, Int>>(container)
        assertEquals("42", adapter.externalTransformState(42))
    }
}
