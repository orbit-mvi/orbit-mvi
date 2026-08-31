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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.orbitContainer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ContextParametersTest {
    private val initialState = InternalState(Random.nextInt())
    private val incrementedCount = initialState.count + 1

    @Test
    fun internal_state_test_scope_resolved_from_context() = runTest {
        createMiddleware().testWithInternalState {
            containerHost.increment()

            expectInternalState { InternalState(incrementedCount) }
            expectSideEffect(incrementedCount)
        }
    }

    @Test
    fun external_state_test_scope_resolved_from_context() = runTest {
        createMiddleware().testWithExternalState {
            containerHost.increment()

            expectExternalState { ExternalState(incrementedCount.toString()) }
            expectSideEffect(incrementedCount)
        }
    }

    @Test
    fun internal_and_external_state_test_scope_resolved_from_context() = runTest {
        createMiddleware().testWithInternalAndExternalState {
            containerHost.increment()

            expectInternalState { InternalState(incrementedCount) }
            expectExternalState { ExternalState(incrementedCount.toString()) }
            expectSideEffect(incrementedCount)
        }
    }

    @Test
    fun context_overload_accepts_initial_state_and_settings() = runTest {
        val seededState = InternalState(Random.nextInt())

        createMiddleware().testWithInternalState(
            initialState = seededState,
            settings = TestSettings(autoCheckInitialState = false)
        ) {
            expectInternalState { seededState }
        }
    }

    @Test
    fun context_overload_runs_on_create_on_request() = runTest {
        val middleware = createMiddleware()

        middleware.testWithInternalState {
            runOnCreate().join()

            assertEquals(true, middleware.createCalled)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun explicit_test_scope_overload_still_resolves_when_a_test_scope_is_in_context() = runTest {
        val scope = TestScope()

        createMiddleware().testWithInternalState(scope) {
            containerHost.incrementAfterDelay()

            // The intent is delayed, so nothing is emitted until we advance the separate scope's virtual time
            expectNoItems()

            scope.testScheduler.advanceTimeBy(DELAY_MILLIS + 1)

            expectInternalState { InternalState(incrementedCount) }
            expectSideEffect(incrementedCount)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun explicit_test_scope_external_state_overload_still_resolves_when_a_test_scope_is_in_context() = runTest {
        val scope = TestScope()

        createMiddleware().testWithExternalState(scope) {
            containerHost.incrementAfterDelay()

            // The intent is delayed, so nothing is emitted until we advance the separate scope's virtual time
            expectNoItems()

            scope.testScheduler.advanceTimeBy(DELAY_MILLIS + 1)

            expectExternalState { ExternalState(incrementedCount.toString()) }
            expectSideEffect(incrementedCount)
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun explicit_test_scope_internal_and_external_state_overload_still_resolves_when_a_test_scope_is_in_context() = runTest {
        val scope = TestScope()

        createMiddleware().testWithInternalAndExternalState(scope) {
            containerHost.incrementAfterDelay()

            // The intent is delayed, so nothing is emitted until we advance the separate scope's virtual time
            expectNoItems()

            scope.testScheduler.advanceTimeBy(DELAY_MILLIS + 1)

            expectInternalState { InternalState(incrementedCount) }
            expectExternalState { ExternalState(incrementedCount.toString()) }
            expectSideEffect(incrementedCount)
        }
    }

    private fun TestScope.createMiddleware(): GeneralTestMiddleware = GeneralTestMiddleware(backgroundScope)

    private inner class GeneralTestMiddleware(coroutineScope: CoroutineScope) :
        OrbitContainerHost<InternalState, ExternalState, Int> {

        var createCalled = false

        override val container: OrbitContainer<InternalState, ExternalState, Int> =
            coroutineScope.orbitContainer(initialState, ::transformState, onCreate = { createCalled = true })

        private fun transformState(internalState: InternalState) = ExternalState(internalState.count.toString())

        fun increment() = intent {
            val newCount = state.count + 1
            reduce { InternalState(newCount) }
            postSideEffect(newCount)
        }

        fun incrementAfterDelay() = intent {
            delay(DELAY_MILLIS)
            val newCount = state.count + 1
            reduce { InternalState(newCount) }
            postSideEffect(newCount)
        }
    }

    private data class InternalState(val count: Int)

    private data class ExternalState(val count: String)

    private companion object {
        const val DELAY_MILLIS = 3000L
    }
}
