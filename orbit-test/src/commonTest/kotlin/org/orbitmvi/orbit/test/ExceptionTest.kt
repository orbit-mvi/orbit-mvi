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

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFails

class ExceptionTest {

    private val initialState = State()

    @Test
    fun exceptions_thrown_during_test_can_be_asserted_on() {
        assertFails {
            runTest {
                ExceptionTestMiddleware(this).test(this) {
                    expectInitialState()

                    val job = containerHost.boom()

                    job.join()
                }
            }
        }
    }

    private inner class ExceptionTestMiddleware(scope: TestScope) : ContainerHost<State, Int> {
        override val container = scope.backgroundScope.container<State, Int>(initialState)

        fun boom() = intent {
            throw IllegalStateException("Boom!")
        }
    }

    private data class State(
        val count: Int = Random.nextInt(),
        val list: List<Int> = emptyList()
    )
}
