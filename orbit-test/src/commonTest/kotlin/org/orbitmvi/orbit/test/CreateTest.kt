/*
 * Copyright 2021-2023 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.test

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.container

@ExperimentalCoroutinesApi
internal class CreateTest {
    private val initialState = State(Random.nextInt())

    @OptIn(OrbitExperimental::class)
    @Test
    fun `created is not invoked by default`() = runTest {
        val testSubject = GeneralTestMiddleware(this)

        testSubject.test(this, initialState = initialState) {
            expectInitialState()

            testSubject.container.joinIntents()

            assertEquals(false, testSubject.createCalled)
        }
    }

    @Test
    fun `created is invoked upon request`() = runTest {
        val testSubject = GeneralTestMiddleware(this)

        testSubject.test(this, initialState = initialState) {
            expectInitialState()

            val job = runOnCreate()

            job.join()

            assertEquals(true, testSubject.createCalled)
        }
    }

    @Test
    fun `initial state can be omitted from test`() = runTest {
        val testSubject = GeneralTestMiddleware(this)
        testSubject.test(this) {
            expectState(initialState)
        }
    }

    private inner class GeneralTestMiddleware(scope: TestScope) :
        ContainerHost<State, Nothing> {
        var createCalled = false
        override val container = scope.backgroundScope.container<State, Nothing>(initialState) {
            createCalled = true
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
