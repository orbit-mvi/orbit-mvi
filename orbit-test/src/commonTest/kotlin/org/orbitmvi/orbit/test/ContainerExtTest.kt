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

package org.orbitmvi.orbit.test

import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import org.orbitmvi.orbit.internal.RealContainer
import org.orbitmvi.orbit.orbitContainer
import org.orbitmvi.orbit.withExternalState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(OrbitInternal::class)
class ContainerExtTest {

    @Test
    fun find_transform_state_returns_the_test_container_transform() = runTest {
        val container = backgroundScope.orbitContainer<Int, String, Int>(
            initialState = 1,
            transformState = { it.toString() }
        )

        assertEquals("42", container.findTransformState()(42))
    }

    @Suppress("DEPRECATION")
    @Test
    fun find_transform_state_composes_the_adapter_transform() = runTest {
        val container = backgroundScope.orbitContainer<Int, Int>(initialState = 1)
            .withExternalState { it.toString() }

        assertEquals("42", container.findTransformState()(42))
    }

    @Test
    fun find_transform_state_unwraps_container_decorators() = runTest {
        val container = LazyCreateContainerDecorator<Int, String, Int>(
            actual = backgroundScope.orbitContainer(
                initialState = 1,
                transformState = { it.toString() }
            )
        ) {}

        assertEquals("42", container.findTransformState()(42))
    }

    @Test
    fun find_transform_state_fails_when_no_test_container_is_found() = runTest {
        val container = RealContainer<Int, String, Int>(
            initialState = 1,
            parentScope = backgroundScope,
            settings = RealSettings(),
            transformState = { it.toString() }
        )

        assertFailsWith<IllegalStateException> { container.findTransformState() }
    }
}
