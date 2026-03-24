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

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.internal.ExternalStateContainerAdapter
import org.orbitmvi.orbit.orbitContainer
import org.orbitmvi.orbit.withExternalState
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests that the deprecated [withExternalState] path works correctly with the test framework,
 * covering [ExternalStateContainerAdapter] and the adapter branches
 * in [findOnCreate] and [findTestContainer].
 */
@Suppress("DEPRECATION")
@OptIn(OrbitInternal::class)
class DeprecatedWithExternalStateTest {

    private val initialState = InternalState()

    @Test
    fun with_external_state_wraps_container_in_adapter() = runTest {
        val middleware = MiddlewareWithDeprecatedExternalState(this)
        assertIs<ExternalStateContainerAdapter<InternalState, ExternalState, Int>>(middleware.container)
    }

    @Test
    fun internal_state_flows_through_adapter() = runTest {
        MiddlewareWithDeprecatedExternalState(this).testWithInternalState(this) {
            containerHost.newCount(42)
            expectInternalState { copy(count = 42) }
        }
    }

    @Test
    fun external_state_is_transformed_through_adapter() = runTest {
        val middleware = MiddlewareWithDeprecatedExternalState(this)
        val adapter = middleware.container as ExternalStateContainerAdapter<InternalState, ExternalState, Int>
        assertEquals(initialState, adapter.stateFlow.value)
        assertEquals(ExternalState(initialState.count.toString()), adapter.externalStateFlow.value)
    }

    @Test
    fun side_effects_flow_through_adapter() = runTest {
        MiddlewareWithDeprecatedExternalState(this).testWithInternalState(this) {
            containerHost.newSideEffect(99)
            expectSideEffect(99)
        }
    }

    @Test
    fun on_create_is_found_through_adapter() = runTest {
        MiddlewareWithDeprecatedExternalStateAndOnCreate(this).testWithInternalState(
            this,
            initialState = initialState,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            expectInternalState { initialState }
            runOnCreate()
            expectInternalState { copy(count = -1) }
        }
    }

    @Test
    fun find_test_container_traverses_adapter() = runTest {
        val middleware = MiddlewareWithDeprecatedExternalState(this)
        val testContainer = middleware.container.findTestContainer()
        assertEquals(initialState, testContainer.originalInitialState)
    }

    private inner class MiddlewareWithDeprecatedExternalState(scope: TestScope) :
        OrbitContainerHost<InternalState, ExternalState, Int> {
        override val container: OrbitContainer<InternalState, ExternalState, Int> =
            scope.backgroundScope.orbitContainer<InternalState, Int>(initialState)
                .withExternalState { ExternalState(it.count.toString()) }

        fun newCount(action: Int) = intent {
            reduce { state.copy(count = action) }
        }

        fun newSideEffect(action: Int) = intent {
            postSideEffect(action)
        }
    }

    private inner class MiddlewareWithDeprecatedExternalStateAndOnCreate(scope: TestScope) :
        OrbitContainerHost<InternalState, ExternalState, Int> {
        override val container: OrbitContainer<InternalState, ExternalState, Int> =
            scope.backgroundScope.orbitContainer<InternalState, Int>(
                initialState,
                onCreate = { reduce { state.copy(count = -1) } }
            ).withExternalState { ExternalState(it.count.toString()) }

        fun newCount(action: Int) = intent {
            reduce { state.copy(count = action) }
        }
    }

    private data class InternalState(val count: Int = Random.nextInt())

    private data class ExternalState(val count: String)
}
