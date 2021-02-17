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

package org.orbitmvi.orbit.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.test
import org.orbitmvi.orbit.test.assertContainExactly
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class StateTest {

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun `initial state is emitted on connection`() {
        val initialState = TestState()
        val middleware = Middleware(initialState)
        val testStateObserver = middleware.container.stateFlow.test()

        testStateObserver.awaitCount(1)

        testStateObserver.values.assertContainExactly(initialState)
    }

    @Test
    fun `latest state is emitted on connection`() {
        val initialState = TestState()
        val middleware = Middleware(initialState)
        val testStateObserver = middleware.container.stateFlow.test()
        val action = Random.nextInt()
        middleware.something(action)
        testStateObserver.awaitCount(2) // block until the state is updated

        val testStateObserver2 = middleware.container.stateFlow.test()
        testStateObserver2.awaitCount(1)

        testStateObserver.values.assertContainExactly(
            initialState,
            TestState(action)
        )
        testStateObserver2.values.assertContainExactly(
            TestState(
                action
            )
        )
    }

    @Test
    fun `current state is set to the initial state after instantiation`() {
        val initialState = TestState()
        val middleware = Middleware(initialState)

        assertEquals(initialState, middleware.container.currentState)
    }

    @Test
    fun `current state is up to date after modification`() {
        val initialState = TestState()
        val middleware = Middleware(initialState)
        val action = Random.nextInt()
        val testStateObserver = middleware.container.stateFlow.test()

        middleware.something(action)

        testStateObserver.awaitCount(2)

        assertEquals(testStateObserver.values.last(), middleware.container.currentState)
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware(initialState: TestState) : ContainerHost<TestState, String> {
        override val container = scope.container<TestState, String>(initialState)

        fun something(action: Int) = intent {
            reduce {
                state.copy(id = action)
            }
        }
    }
}
