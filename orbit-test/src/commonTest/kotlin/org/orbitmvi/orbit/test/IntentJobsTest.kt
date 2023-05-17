/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.coroutines.coroutineContext
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class IntentJobsTest {

    private val initialState = 0

    @Test
    fun `unfinished intents at the end of the test cause the test to fail`() {
        assertFails {
            runTest {
                IntentJobsMiddleware(this).test(this) {
                    expectInitialState()

                    invokeIntent { infiniteIntent() }
                }
            }
        }
    }

    @Test
    fun `unfinished intents at the end of the test may be ignored`() = runTest {
        IntentJobsMiddleware(this).test(this) {
            expectInitialState()

            invokeIntent { infiniteIntent() }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `intents may be cancelled`() = runTest {
        IntentJobsMiddleware(this).test(this) {
            expectInitialState()

            val job = invokeIntent { infiniteIntent() }

            job.cancel()
        }
    }

    @Test
    fun `intents may be joined`() = runTest {
        val scope = TestScope()
        IntentJobsMiddleware(this).test(scope) {
            expectInitialState()

            val job = invokeIntent { longIntent() }

            scope.testScheduler.advanceTimeBy(300)

            launch {
                job.join()
            }
            assertTrue { job.isActive }
            scope.testScheduler.advanceTimeBy(1)
            assertTrue { job.isCompleted }
            expectState { this + 2 }
        }
    }

    @Test
    fun `onCreate may be joined`() = runTest {
        val scope = TestScope()
        IntentJobsMiddleware(this).test(scope) {
            expectInitialState()

            val job = runOnCreate()

            scope.testScheduler.advanceTimeBy(300)

            launch {
                job.join()
            }
            assertTrue { job.isActive }
            scope.testScheduler.advanceTimeBy(1)
            assertTrue { job.isCompleted }
            expectState { this + 2 }
        }
    }

    @Test
    fun `onCreate may be cancelled`() = runTest {
        val scope = TestScope()
        IntentJobsMiddleware(this).test(scope) {
            expectInitialState()

            val job = runOnCreate()

            scope.testScheduler.advanceTimeBy(200)

            job.cancel()
            assertTrue { job.isCancelled }
            scope.testScheduler.advanceTimeBy(1)
            assertTrue { job.isCompleted }
        }
    }

    private inner class IntentJobsMiddleware(scope: TestScope) : ContainerHost<Int, Int> {
        override val container = scope.backgroundScope.container<Int, Int>(initialState) {
            delay(300)
            reduce { state + 2 }
        }

        fun infiniteIntent() = intent {
            while (coroutineContext.isActive) {
                delay(3000)
                reduce { state + 1 }
            }
        }

        fun longIntent() = intent {
            delay(300)
            reduce { state + 2 }
        }
    }
}
