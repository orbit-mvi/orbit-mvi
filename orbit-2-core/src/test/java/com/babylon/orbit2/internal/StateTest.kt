/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2.internal

import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.syntax.strict.reduce
import com.babylon.orbit2.test
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class StateTest {

    @Test
    fun `initial state is emitted on connection`() {
        val initialState = TestState()
        val middleware = Middleware(initialState)
        val testStateObserver = middleware.container.stateFlow.test()

        testStateObserver.awaitCount(1)

        testStateObserver.values.shouldContainExactly(initialState)
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

        testStateObserver.values.shouldContainExactly(
            initialState,
            TestState(action)
        )
        testStateObserver2.values.shouldContainExactly(
            TestState(
                action
            )
        )
    }

    @Test
    fun `current state is set to the initial state after instantiation`() {
        val initialState = TestState()
        val middleware = Middleware(initialState)

        middleware.container.currentState.shouldBe(initialState)
    }

    @Test
    fun `current state is up to date after modification`() {
        val initialState = TestState()
        val middleware = Middleware(initialState)
        val action = Random.nextInt()
        val testStateObserver = middleware.container.stateFlow.test()

        middleware.something(action)

        testStateObserver.awaitCount(2)

        middleware.container.currentState.shouldBe(testStateObserver.values.last())
    }

    private data class TestState(val id: Int = Random.nextInt())

    private class Middleware(initialState: TestState) : ContainerHost<TestState, String> {
        override val container =
            CoroutineScope(Dispatchers.Unconfined).container<TestState, String>(initialState)

        fun something(action: Int) = orbit {
            reduce {
                state.copy(id = action)
            }
        }
    }
}
