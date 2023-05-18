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

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class InitTest {
    private val initialState = State(Random.nextInt())

    @Test
    fun `created is not invoked by default`() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).test(this, initialState) {
            expectInitialState()
        }

        assertEquals(false, mockDependency.createCalled.value)
    }

    @Test
    fun `created is invoked upon request`() = runTest {
        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).test(this, initialState = initialState) {
            expectInitialState()
            runOnCreate()
        }

        assertEquals(true, mockDependency.createCalled.value)
    }

    @Test
    fun `initial state can be omitted from test`() = runTest {
        createMiddleware().test(this) {
            assertEquals(initialState, awaitState())
        }
    }

    private fun TestScope.createMiddleware(dependency: BogusDependency = FakeDependency()): GeneralTestMiddleware {
        return GeneralTestMiddleware(this.backgroundScope, dependency)
    }

    private inner class GeneralTestMiddleware(coroutineScope: CoroutineScope, val dependency: BogusDependency) :
        ContainerHost<State, Nothing> {
        override val container = coroutineScope.container<State, Nothing>(initialState) {
            created()
        }

        fun created() {
            dependency.create()
            println("created!")
        }
    }

    private data class State(val count: Int = Random.nextInt())

    private interface BogusDependency {
        fun create()
    }

    private class FakeDependency : BogusDependency {
        val createCalled = atomic(false)

        override fun create() {
            createCalled.compareAndSet(expect = false, update = true)
        }
    }
}
