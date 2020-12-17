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
import com.babylon.orbit2.syntax.strict.sideEffect
import com.babylon.orbit2.test
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class ContainerLifecycleTest {

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun `onCreate is called once after connecting to the container`() {
        val initialState = TestState()
        val middleware = Middleware(initialState)
        val testStateObserver = middleware.container.stateFlow.test()
        val testSideEffectObserver = middleware.container.sideEffectFlow.test()

        testStateObserver.awaitCount(1)
        testSideEffectObserver.awaitCount(1)

        testStateObserver.values.shouldContainExactly(initialState)
        testSideEffectObserver.values.shouldContainExactly(initialState.id.toString())
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware(initialState: TestState) : ContainerHost<TestState, String> {

        override var container = scope.container<TestState, String>(initialState) {
            onCreate(it)
        }

        private fun onCreate(createState: TestState) = orbit {
            sideEffect {
                post(createState.id.toString())
            }
        }
    }
}
