/*
 * Copyright 2021-2022 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.test

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent

@ExperimentalCoroutinesApi
internal class GeneralTest {
    private val initialState = State(Random.nextInt())

    @Test
    fun `created is not invoked by default`() = runTest {
        val mockDependency = FakeDependency()
        val testSubject = createMiddleware(mockDependency)

        testSubject.turbineTest(initialState) {
            expectInitialState()
        }

        assertEquals(false, mockDependency.createCalled.value)
    }

    @Test
    fun `created is invoked upon request`() = runTest {

        val mockDependency = FakeDependency()
        val testSubject = createMiddleware(mockDependency).turbineTestIn(this, initialState = initialState)

        testSubject.expectInitialState()
        testSubject.runOnCreate()
        testSubject.cancel()

        assertEquals(true, mockDependency.createCalled.value)
    }

    @Test
    fun `created is invoked upon request 2`() = runTest {

        val mockDependency = FakeDependency()
        val testSubject = createMiddleware(mockDependency)

        testSubject.turbineTest(initialState = initialState) {
            expectInitialState()
            runOnCreate()
        }

        assertEquals(true, mockDependency.createCalled.value)
    }

    @Test
    fun `created is not invoked by default in live test`() = runTest {

        val mockDependency = FakeDependency()
        createMiddleware(mockDependency).turbineLiveTest(initialState = initialState) {
            expectInitialState()
            assertEquals(false, mockDependency.createCalled.value)
        }
    }

    @Test
    fun `created is invoked upon request in live test`() = runTest {

        val mockDependency = FakeDependency()
        val testSubject = createMiddleware(mockDependency).turbineLiveTestIn(this, initialState = initialState)

        testSubject.runOnCreate()

        testSubject.expectInitialState()
        testSubject.cancel()

        assertEquals(true, mockDependency.createCalled.value)
    }

    @Test
    fun `first intent is not isolated by default`() = runTest {

        val mockDependency = FakeDependency()
        val testSubject = createMiddleware(mockDependency)
        val testContainerHost = testSubject.turbineTestIn(this, initialState = initialState)

        testContainerHost.expectInitialState()
        testContainerHost.invokeIntent { something() }
        testContainerHost.cancel()

        assertEquals(true, (testSubject.dependency as FakeDependency).something1Called.value)
        assertEquals(true, testSubject.dependency.something2Called.value)
    }

    @Test
    fun `first intent can be isolated`() = runTest {

        val mockDependency = FakeDependency()
        val testSubject = createMiddleware(mockDependency)
        val testContainerHost = testSubject.turbineTestIn(this, initialState = initialState, buildSettings = { isolateFlow = true })

        testContainerHost.invokeIntent { something() }
        testContainerHost.expectInitialState()

        assertEquals(true, (testSubject.dependency as FakeDependency).something1Called.value)
        assertEquals(false, testSubject.dependency.something2Called.value)
        testContainerHost.cancel()
    }

    @Test
    fun `initial state can be omitted from test`() = runTest {

        val testSubject = createMiddleware()
        val testContainerHost = testSubject.turbineTestIn(this)

        val state = testContainerHost.awaitState()
        testContainerHost.cancel()

        assertEquals(initialState, state)
    }

    private fun TestScope.createMiddleware(dependency: BogusDependency = FakeDependency()): GeneralTestMiddleware {
        return GeneralTestMiddleware(this, dependency)
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
        val createCalled = atomic(false)
        val something1Called = atomic(false)
        val something2Called = atomic(false)

        override fun create() {
            createCalled.compareAndSet(expect = false, update = true)
        }

        override fun something1() {
            something1Called.compareAndSet(expect = false, update = true)
        }

        override fun something2() {
            something2Called.compareAndSet(expect = false, update = true)
        }
    }
}
