/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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
 */

package org.orbitmvi.orbit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
internal class SuspendingStrategyDispatchTest {
    private val initialState = State()

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun `suspending test maintains test dispatcher through runBlockingTest`() = runBlockingTest {
        val executionTime = measureTimeMillis {
            val initAction = Random.nextInt()
            val action = Random.nextInt()

            val testSubject = StateTestMiddleware(initAction).test(initialState)

            testSubject.runOnCreate()
            testSubject.testIntent { somethingInBackground(action) }

            testSubject.assert(initialState) {
                states(
                    { copy(count = initAction) },
                    { copy(count = action) },
                )
            }
        }
        assertTrue { executionTime < 1000 }
    }

    private inner class StateTestMiddleware(initAction: Int) :
        ContainerHost<State, Nothing> {
        override val container = scope.container<State, Nothing>(initialState) {
            intent {
                delay(5000)
                reduce {
                    State(count = initAction)
                }
            }
        }

        fun somethingInBackground(action: Int): Unit = intent {
            delay(5000)
            reduce {
                State(count = action)
            }
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
