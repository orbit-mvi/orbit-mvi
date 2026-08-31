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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.annotation.OrbitExperimental
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(OrbitExperimental::class)
class OrbitContainerHostExtensionsTest {

    @Test
    fun `state and side effect types are inferred from the host`() = runTest {
        // The container declares no type arguments, so this only compiles if they are inferred
        // from the host declaration.
        val host = TestHost(backgroundScope)

        val expected: OrbitContainer<TestState, TestState, Int> = host.container
        assertEquals(TestState(0), expected.stateFlow.value)
    }

    @Test
    fun `settings are applied to the created container`() = runTest {
        val host = TestHost(backgroundScope)

        assertEquals(SideEffectMode.BROADCAST, host.container.settings.sideEffectMode)
    }

    @Test
    fun `container created from the host works`() = runTest {
        val host = TestHost(backgroundScope)

        host.container.sideEffectFlow.test {
            val value = Random.nextInt(1, 65535)
            host.newSideEffect(value)

            assertEquals(value, awaitItem())
            assertEquals(TestState(value), host.container.stateFlow.value)
        }
    }

    @Test
    fun `onCreate is executed for the host overload`() = runTest {
        val host = OnCreateHost(backgroundScope)

        host.container.stateFlow.test {
            assertEquals(TestState(0), awaitItem())
            assertEquals(TestState(1), awaitItem())
        }
    }

    @Test
    fun `internal and external state types are inferred from the host`() = runTest {
        val host = ExternalStateTestHost(backgroundScope)

        val expected: OrbitContainer<Int, String, Int> = host.container
        assertEquals(0, expected.stateFlow.value)

        host.container.externalStateFlow.test {
            assertEquals("0", awaitItem())

            host.newState(42)

            assertEquals("42", awaitItem())
        }
    }

    data class TestState(val value: Int)

    private class TestHost(scope: CoroutineScope) : OrbitContainerHost<TestState, TestState, Int> {
        override val container = orbitContainer(
            scope = scope,
            initialState = TestState(0),
            buildSettings = { sideEffectMode = SideEffectMode.BROADCAST }
        )

        fun newSideEffect(action: Int) = intent {
            reduce { TestState(action) }
            postSideEffect(action)
        }
    }

    private class OnCreateHost(scope: CoroutineScope) : OrbitContainerHost<TestState, TestState, Int> {
        override val container = orbitContainer(scope, TestState(0)) {
            reduce { TestState(1) }
        }
    }

    private class ExternalStateTestHost(scope: CoroutineScope) : OrbitContainerHost<Int, String, Int> {
        override val container = orbitContainer(
            scope = scope,
            initialState = 0,
            transformState = Int::toString
        )

        fun newState(action: Int) = intent { reduce { action } }
    }
}
