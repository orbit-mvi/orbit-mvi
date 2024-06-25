/*
 * Copyright 2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RunOnTest {

    @Test
    fun run_on_does_not_run_when_state_does_not_match_expected() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            val job = middleware.doIfInReadyState()

            job.join()

            expectNoItems()
        }
    }

    @Test
    fun run_on_runs_when_state_matches_expected() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            middleware.changeToState(TestState.Ready(42))
            expectState { TestState.Ready(42) }

            middleware.doIfInReadyState()
            expectState { TestState.Ready(43) }
        }
    }

    @Test
    fun run_on_does_not_run_when_state_matches_expected_but_predicate_does_not_match() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            middleware.changeToState(TestState.Ready(42))
            expectState { TestState.Ready(42) }

            val job = middleware.doIfInReadyState(predicate = { it.id % 2 == 1 })

            job.join()
            expectNoItems()
        }
    }

    @Test
    fun run_on_runs_when_state_matches_expected_and_predicate_matches() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            middleware.changeToState(TestState.Ready(42))
            expectState { TestState.Ready(42) }

            val job = middleware.doIfInReadyState(predicate = { it.id % 2 == 0 })
            expectState { TestState.Ready(43) }

            job.join()
            expectNoItems()
        }
    }

    @Test
    fun run_on_cancels_when_state_stops_matching_expected() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            middleware.changeToState(TestState.Ready(42))
            expectState { TestState.Ready(42) }

            val job = middleware.collectIfInReadyState()

            middleware.channel.send(123) // Sending something to ensure intent is started
            assertEquals(123, middleware.collectorChannel.receive())

            middleware.changeToState(TestState.Loading)
            expectState { TestState.Loading }

            job.join() // This will never finish if the `collect` is still running
        }
    }

    @Test
    fun run_on_cancels_when_predicate_stops_matching() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            middleware.changeToState(TestState.Ready(42))
            expectState { TestState.Ready(42) }

            val job = middleware.collectIfInReadyState(predicate = { it.id % 2 == 0 })

            middleware.channel.send(123) // Sending something to ensure intent is started
            assertEquals(123, middleware.collectorChannel.receive())

            middleware.changeToState(TestState.Ready(43))
            expectState { TestState.Ready(43) }

            job.join() // This will never finish if the `collect` is still running
        }
    }

    @Test
    fun run_on_does_not_cancel_when_predicate_keeps_matching_on_subsequent_emissions() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            middleware.changeToState(TestState.Ready(42))
            expectState { TestState.Ready(42) }

            val job = middleware.collectIfInReadyState(predicate = { it.id % 2 == 0 })

            middleware.channel.send(123) // Sending something to ensure intent is started
            assertEquals(123, middleware.collectorChannel.receive())

            middleware.changeToState(TestState.Ready(50))
            expectState { TestState.Ready(50) }

            assertTrue(job.isActive)
            job.cancel()
        }
    }

    sealed interface TestState {
        object Loading : TestState
        data class Ready(val id: Int = 42) : TestState
    }

    @OptIn(OrbitExperimental::class)
    private inner class Middleware(scope: CoroutineScope) : ContainerHost<TestState, String> {
        override val container = scope.container<TestState, String>(TestState.Loading)

        val channel: Channel<Int> = Channel()
        val collectorChannel: Channel<Int> = Channel()

        fun changeToState(state: TestState) = intent {
            reduce {
                state
            }
        }

        fun doIfInReadyState(predicate: (TestState.Ready) -> Boolean = { true }) = intent {
            runOn<TestState.Ready>(predicate = predicate) {
                reduce {
                    state.copy(id = state.id + 1)
                }
            }
        }

        fun collectIfInReadyState(predicate: (TestState.Ready) -> Boolean = { true }) = intent {
            runOn<TestState.Ready>(predicate = predicate) {
                channel.consumeAsFlow()
                    .collect(collectorChannel::send)
            }
        }
    }
}
