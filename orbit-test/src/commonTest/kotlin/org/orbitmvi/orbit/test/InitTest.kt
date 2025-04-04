/*
 * Copyright 2023-2025 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InitTest {
    private val initialState = State(Random.nextInt())

    @Test
    fun created_is_not_invoked_by_default() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).test(this, initialState, settings = TestSettings(autoCheckInitialState = false)) {
            expectState { initialState }
        }

        assertEquals(false, mockDependency.createCalled.load())
    }

    @Test
    fun created_is_invoked_upon_request() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).test(this, initialState = initialState, settings = TestSettings(autoCheckInitialState = false)) {
            expectState { initialState }
            runOnCreate()
        }

        assertEquals(true, mockDependency.createCalled.load())
    }

    @Test
    fun initial_state_can_be_explicitly_checked_in_test_with_awaitState() = runTest {
        createMiddleware().test(this, settings = TestSettings(autoCheckInitialState = false)) {
            assertEquals(initialState, awaitState())
        }
    }

    @Test
    fun initial_state_can_be_explicitly_checked_in_test_with_expectInitialState() = runTest {
        createMiddleware().test(this, settings = TestSettings(autoCheckInitialState = false)) {
            @Suppress("DEPRECATION")
            expectInitialState()
        }
    }

    @Test
    fun initial_state_can_be_explicitly_checked_in_test_with_expectState() = runTest {
        createMiddleware().test(this, settings = TestSettings(autoCheckInitialState = false)) {
            expectState { initialState }
        }
    }

    @Test
    fun initial_state_can_be_omitted_from_test() = runTest {
        createMiddleware().test(this) {
        }
    }

    private fun TestScope.createMiddleware(dependency: BogusDependency = FakeDependency()): GeneralTestMiddleware {
        return GeneralTestMiddleware(this.backgroundScope, dependency)
    }

    private inner class GeneralTestMiddleware(coroutineScope: CoroutineScope, val dependency: BogusDependency) :
        ContainerHost<State, Nothing> {
        override val container = coroutineScope.container<State, Nothing>(initialState) {
            created()
        }

        fun created() {
            dependency.create()
        }
    }

    private data class State(val count: Int = Random.nextInt())

    private interface BogusDependency {
        fun create()
    }

    private class FakeDependency : BogusDependency {
        val createCalled = AtomicBoolean(false)

        override fun create() {
            createCalled.compareAndSet(expectedValue = false, newValue = true)
        }
    }
}
