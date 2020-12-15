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

package com.babylon.orbit2

import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.syntax.strict.sideEffect
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScope
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class GeneralTest {
    private val initialState = State()
    private val scope by lazy { TestCoroutineScope(Job()) }

    @AfterTest
    fun afterTest() {
        scope.cleanupTestCoroutines()
        scope.cancel()
    }

    @Test
    fun `created is not invoked by default`() {

        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(mockDependency)

        testSubject.test(initialState)

        mockDependency.createCallCount.shouldBe(0)
    }

    @Test
    fun `created is invoked upon request`() {

        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(mockDependency)

        testSubject.test(initialState = initialState, runOnCreate = true)

        mockDependency.createCallCount.shouldBe(1)
    }

    @Test
    fun `first flow is isolated by default`() {

        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(mockDependency)

        val spy = testSubject.test(initialState)

        spy.something()

        mockDependency.something1CallCount.shouldBe(1)
        mockDependency.something2CallCount.shouldBe(0)
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

        fun something() = orbit {
            sideEffect { dependency.something1() }
                .sideEffect { somethingElse() }
        }

        fun somethingElse() = orbit {
            sideEffect { dependency.something2() }
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
