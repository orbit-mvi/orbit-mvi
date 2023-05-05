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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertTrue

@Suppress("DEPRECATION")
@ExperimentalCoroutinesApi
internal class SuspendingStrategyDispatchTest {
    private val initialState = State()

    @Test
    fun `suspending test maintains test dispatcher through runTest`() = runTest {
        val executionTime = measureTimeMillis {
            val initAction = Random.nextInt()
            val action = Random.nextInt()

            val testSubject = StateTestMiddleware(this, initAction).test(initialState)

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

    private inner class StateTestMiddleware(scope: TestScope, initAction: Int) :
        ContainerHost<State, Nothing> {
        override val container = scope.backgroundScope.container<State, Nothing>(initialState) {
            intent {
                delay(5000)
                reduce {
                    State(count = initAction)
                }
            }
        }

        fun somethingInBackground(action: Int) = intent {
            delay(5000)
            reduce {
                State(count = action)
            }
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
