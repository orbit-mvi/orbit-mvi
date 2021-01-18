/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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

package org.orbitmvi.orbit

import org.orbitmvi.orbit.syntax.strict.orbitDslPlugins
import org.orbitmvi.orbit.syntax.strict.orbit
import org.orbitmvi.orbit.syntax.strict.reduce
import org.orbitmvi.orbit.syntax.strict.sideEffect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orbitmvi.orbit.coroutines.transformFlow
import org.orbitmvi.orbit.coroutines.transformSuspend
import kotlin.random.Random

@ExperimentalCoroutinesApi
internal class CoroutineDslPluginBehaviourTest {
    private val initialState = TestState()
    private val scope = TestCoroutineScope(Job())

    @AfterEach
    fun afterEach() {
        scope.cleanupTestCoroutines()
        scope.cancel()
    }

    @BeforeEach
    fun beforeEach() {
        orbitDslPlugins.reset() // Test for proper registration
    }

    @Test
    fun `suspend transformation maps`() {
        val action = Random.nextInt()
        val middleware = Middleware().test(initialState)

        middleware.suspend(action)

        middleware.assert(initialState) {
            states(
                { TestState(action + 5) }
            )
        }
    }

    @Test
    fun `flow transformation flatmaps`() {
        val action = Random.nextInt()
        val middleware = Middleware().test(initialState)

        middleware.flow(action)

        middleware.assert(initialState) {
            states(
                { TestState(action) },
                { TestState(action + 1) },
                { TestState(action + 2) },
                { TestState(action + 3) }
            )
        }
    }

    @Test
    fun `hot flow transformation flatmaps`() {
        val action = Random.nextInt()
        val channel = Channel<Int>(100)
        val middleware = Middleware(channel.consumeAsFlow()).test(initialState = initialState, blocking = false)

        middleware.hotFlow()

        channel.sendBlocking(action)
        channel.sendBlocking(action + 1)
        channel.sendBlocking(action + 2)
        channel.sendBlocking(action + 3)

        middleware.assert(initialState) {
            postedSideEffects(
                action.toString(),
                (action + 1).toString(),
                (action + 2).toString(),
                (action + 3).toString()
            )
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware(val hotFlow: Flow<Int> = emptyFlow()) : ContainerHost<TestState, String> {

        override var container = scope.container<TestState, String>(TestState(42))

        fun suspend(action: Int) = orbit {
            transformSuspend {
                delay(50)
                action + 5
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun flow(action: Int) = orbit {
            transformFlow {
                flowOf(action, action + 1, action + 2, action + 3)
                    .onEach { delay(50) }
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun hotFlow() = orbit {
            transformFlow {
                hotFlow
            }
                .sideEffect {
                    post(event.toString())
                }
        }
    }
}
