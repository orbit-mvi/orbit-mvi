/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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
import org.orbitmvi.orbit.ContainerHostWithExternalState
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.withExternalState
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InitWithExternalStateTest {
    private val initialState = InternalState(Random.nextInt())

    @Test
    fun internal_created_is_not_invoked_by_default() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).testWithInternalState(
            this,
            initialState,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            expectInternalState { initialState }
        }

        assertEquals(false, mockDependency.createCalled.load())
    }

    @Test
    fun external_created_is_not_invoked_by_default() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).testWithExternalState(
            this,
            initialState,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            expectExternalState { ExternalState(initialState.count.toString()) }
        }

        assertEquals(false, mockDependency.createCalled.load())
    }

    @Test
    fun internal_and_external_created_is_not_invoked_by_default() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).testWithInternalAndExternalState(
            this,
            initialState,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            expectInternalState { initialState }
            expectExternalState { ExternalState(initialState.count.toString()) }
        }

        assertEquals(false, mockDependency.createCalled.load())
    }

    @Test
    fun internal_created_is_invoked_upon_request() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).testWithInternalState(
            this,
            initialState = initialState,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            expectInternalState { initialState }
            runOnCreate()
        }

        assertEquals(true, mockDependency.createCalled.load())
    }

    @Test
    fun external_created_is_invoked_upon_request() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).testWithExternalState(
            this,
            initialState = initialState,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            expectExternalState { ExternalState(initialState.count.toString()) }
            runOnCreate()
        }

        assertEquals(true, mockDependency.createCalled.load())
    }

    @Test
    fun internal_and_external_created_is_invoked_upon_request() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).testWithInternalAndExternalState(
            this,
            initialState = initialState,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            expectInternalState { initialState }
            expectExternalState { ExternalState(initialState.count.toString()) }
            runOnCreate()
        }

        assertEquals(true, mockDependency.createCalled.load())
    }

    @Test
    fun internal_initial_state_can_be_explicitly_checked_in_test_with_awaitState() = runTest {
        createMiddleware().testWithInternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
            assertEquals(initialState, awaitInternalState())
        }
    }

    @Test
    fun external_initial_state_can_be_explicitly_checked_in_test_with_awaitState() = runTest {
        createMiddleware().testWithExternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
            assertEquals(ExternalState(initialState.count.toString()), awaitExternalState())
        }
    }

    @Test
    fun internal_and_external_initial_state_can_be_explicitly_checked_in_test_with_awaitState() = runTest {
        createMiddleware().testWithInternalAndExternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
            assertEquals(initialState, awaitInternalState())
            assertEquals(ExternalState(initialState.count.toString()), awaitExternalState())
        }
    }

    @Test
    fun internal_initial_state_can_be_explicitly_checked_in_test_with_expectState() = runTest {
        createMiddleware().testWithInternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
            expectInternalState { initialState }
        }
    }

    @Test
    fun external_initial_state_can_be_explicitly_checked_in_test_with_expectState() = runTest {
        createMiddleware().testWithExternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
            expectExternalState { ExternalState(initialState.count.toString()) }
        }
    }

    @Test
    fun internal_and_external_initial_state_can_be_explicitly_checked_in_test_with_expectState() = runTest {
        createMiddleware().testWithInternalAndExternalState(this, settings = TestSettings(autoCheckInitialState = false)) {
            expectInternalState { initialState }
            expectExternalState { ExternalState(initialState.count.toString()) }
        }
    }

    @Test
    fun internal_initial_state_can_be_omitted_from_test() = runTest {
        createMiddleware().testWithInternalState(this) {
        }
    }

    @Test
    fun external_initial_state_can_be_omitted_from_test() = runTest {
        createMiddleware().testWithInternalState(this) {
        }
    }

    @Test
    fun internal_and_external_initial_state_can_be_omitted_from_test() = runTest {
        createMiddleware().testWithInternalAndExternalState(this) {
        }
    }

    private fun TestScope.createMiddleware(dependency: BogusDependency = FakeDependency()): GeneralTestMiddleware {
        return GeneralTestMiddleware(this.backgroundScope, dependency)
    }

    private inner class GeneralTestMiddleware(coroutineScope: CoroutineScope, val dependency: BogusDependency) :
        ContainerHostWithExternalState<InternalState, ExternalState, Nothing> {
        override val container = coroutineScope.container<InternalState, Nothing>(initialState) {
            created()
        }.withExternalState(::transformState)

        private fun transformState(internalState: InternalState) = ExternalState(internalState.count.toString())

        fun created() {
            dependency.create()
        }
    }

    private data class InternalState(val count: Int = Random.nextInt())
    private data class ExternalState(val count: String)

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
