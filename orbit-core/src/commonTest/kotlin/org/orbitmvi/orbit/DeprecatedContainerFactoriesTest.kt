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

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.Syntax
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The deprecated [CoroutineScope.orbitContainer] and [CoroutineScope.container] factories now
 * delegate to [createOrbitContainer]. They are still public API until they are removed, so pin down
 * that the delegation keeps producing an equivalent container.
 */
@Suppress("DEPRECATION")
@OptIn(OrbitExperimental::class)
class DeprecatedContainerFactoriesTest {

    @Test
    fun `deprecated orbitContainer creates a working container`() = runTest {
        val container = backgroundScope.orbitContainer<TestState, Int>(TestState(0))

        assertEquals(TestState(0), container.stateFlow.value)

        container.sideEffectFlow.test {
            val value = Random.nextInt(1, 65535)
            container.emit(value)

            assertEquals(value, awaitItem())
            assertEquals(TestState(value), container.stateFlow.value)
        }
    }

    @Test
    fun `deprecated orbitContainer honours settings and onCreate`() = runTest {
        val container = backgroundScope.orbitContainer<TestState, Int>(
            initialState = TestState(0),
            buildSettings = { sideEffectMode = SideEffectMode.BROADCAST },
            onCreate = { reduce { TestState(1) } }
        )

        assertEquals(SideEffectMode.BROADCAST, container.settings.sideEffectMode)

        container.stateFlow.test {
            assertEquals(TestState(0), awaitItem())
            assertEquals(TestState(1), awaitItem())
        }
    }

    @Test
    fun `deprecated orbitContainer transforms external state`() = runTest {
        val container = backgroundScope.orbitContainer<Int, String, Int>(
            initialState = 0,
            transformState = Int::toString
        )

        container.externalStateFlow.test {
            assertEquals("0", awaitItem())
        }
    }

    @Test
    fun `deprecated container creates a working container`() = runTest {
        val container = backgroundScope.container<TestState, Int>(TestState(0))

        assertEquals(TestState(0), container.stateFlow.value)

        container.sideEffectFlow.test {
            val value = Random.nextInt(1, 65535)
            container.emit(value)

            assertEquals(value, awaitItem())
            assertEquals(TestState(value), container.stateFlow.value)
        }
    }

    @Test
    fun `deprecated container honours settings and onCreate`() = runTest {
        val container = backgroundScope.container<TestState, Int>(
            initialState = TestState(0),
            buildSettings = { sideEffectMode = SideEffectMode.BROADCAST },
            onCreate = { reduce { TestState(1) } }
        )

        assertEquals(SideEffectMode.BROADCAST, container.settings.sideEffectMode)

        container.stateFlow.test {
            assertEquals(TestState(0), awaitItem())
            assertEquals(TestState(1), awaitItem())
        }
    }

    @Test
    fun `deprecated container transforms external state`() = runTest {
        val container = backgroundScope.container<Int, String, Int>(
            initialState = 0,
            transformState = Int::toString
        )

        container.externalStateFlow.test {
            assertEquals("0", awaitItem())
        }
    }

    data class TestState(val value: Int)

    private companion object {
        fun OrbitContainer<TestState, TestState, Int>.emit(action: Int) = orbit {
            Syntax(this).apply {
                reduce { TestState(action) }
                postSideEffect(action)
            }
        }
    }
}
