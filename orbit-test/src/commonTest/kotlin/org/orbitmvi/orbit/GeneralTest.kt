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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.orbitmvi.orbit.syntax.simple.intent
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class GeneralTest {
    private val initialState = State()
    private val scope by lazy { CoroutineScope(Job()) }

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun `created is not invoked by default`() {

        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(mockDependency)

        testSubject.test(initialState)

        assertEquals(0, mockDependency.createCallCount)
    }

    @Test
    fun `created is invoked upon request`() {

        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(mockDependency)

        testSubject.test(initialState = initialState, runOnCreate = true)

        assertEquals(1, mockDependency.createCallCount)
    }

    @Test
    fun `first flow is isolated by default`() {

        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(mockDependency)

        val spy = testSubject.test(initialState)

        spy.something()

        assertEquals(1, mockDependency.something1CallCount)
        assertEquals(0, mockDependency.something2CallCount)
    }

    private inner class GeneralTestMiddleware(private val dependency: BogusDependency) :
        ContainerHost<State, Nothing> {
        override val container = scope.container<State, Nothing>(initialState) {
            created()
        }

        fun created() {
            dependency.create()
            println("created!")
        }

        fun something() = intent {
            dependency.something1()
            somethingElse()
        }

        fun somethingElse() = intent {
            dependency.something2()
        }
    }

    private data class State(val count: Int = Random.nextInt())

    private interface BogusDependency {
        fun create()
        fun something1()
        fun something2()
    }

    private class FakeDependency : BogusDependency {
        var createCallCount = 0
            private set
        var something1CallCount = 0
            private set
        var something2CallCount = 0
            private set

        override fun create() {
            createCallCount++
        }

        override fun something1() {
            something1CallCount++
        }

        override fun something2() {
            something2CallCount++
        }
    }
}
