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
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.runIfStateMatches
import org.orbitmvi.orbit.test.test
import kotlin.test.Test

internal class RunIfStateMatchesTest {

    @Test
    fun `ifState does not run when state does not match expected`() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            val job = middleware.doIfInReadyState()

            job.join()

            expectNoItems()
        }
    }

    @Test
    fun `ifState runs when state matches expected`() = runTest {
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
    fun `ifState does not run when state matches expected but predicate does not match`() = runTest {
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
    fun `ifState runs when state matches expected and predicate matches`() = runTest {
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
    fun `ifState cancels when state stops matching expected`() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            middleware.changeToState(TestState.Ready(42))
            expectState { TestState.Ready(42) }

            val job = middleware.collectIfInReadyState()

            middleware.channel.send(123) // Sending something to ensure intent is started
            expectState { TestState.Ready(123) }

            middleware.changeToState(TestState.Loading)
            expectState { TestState.Loading }

            job.join() // This will never finish if the `collect` is still running
        }
    }

    @Test
    fun `ifState cancels when predicate stops matching`() = runTest {
        val middleware = Middleware(backgroundScope)

        middleware.test(this) {
            expectState { TestState.Loading }

            middleware.changeToState(TestState.Ready(42))
            expectState { TestState.Ready(42) }

            val job = middleware.collectIfInReadyState(predicate = { it.id % 2 == 0 })

            middleware.channel.send(48) // Sending something to ensure intent is started
            expectState { TestState.Ready(48) }

            middleware.changeToState(TestState.Ready(43))
            expectState { TestState.Ready(43) }

            job.join() // This will never finish if the `collect` is still running
        }
    }

    sealed interface TestState {
        object Loading : TestState
        data class Ready(val id: Int = 42) : TestState
    }

    private inner class Middleware(scope: CoroutineScope) : ContainerHost<TestState, String> {
        override val container = scope.container<TestState, String>(TestState.Loading)

        val channel: Channel<Int> = Channel()

        fun changeToState(state: TestState) = intent {
            reduce {
                state
            }
        }

        fun doIfInReadyState(predicate: (TestState.Ready) -> Boolean = { true }) = intent {
            runIfStateMatches(TestState.Ready::class, predicate = predicate) {
                reduce {
                    state.copy(id = state.id + 1)
                }
            }
        }

        fun collectIfInReadyState(predicate: (TestState.Ready) -> Boolean = { true }) = intent {
            runIfStateMatches(TestState.Ready::class, predicate = predicate) {
                channel.consumeAsFlow()
                    .collect {
                        reduce { state.copy(id = it) }
                    }
            }
        }
    }
}
