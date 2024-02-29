/*
 * Copyright 2021-2023 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.test.assertEventually
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@Suppress("DEPRECATION")
@ExperimentalCoroutinesApi
internal class GeneralTest {
    private val initialState = State(Random.nextInt())

    @Test
    fun created_is_not_invoked_by_default() = runTest {
        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(this, mockDependency)

        testSubject.test(initialState)

        assertEquals(false, mockDependency.createCalled.value)
    }

    @Test
    fun created_is_invoked_upon_request() = runTest {
        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(this, mockDependency)

        testSubject.test(initialState = initialState).runOnCreate()

        assertEquals(true, mockDependency.createCalled.value)
    }

    @Test
    fun created_is_not_invoked_by_default_in_live_test() = runTest {
        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(this, mockDependency)

        testSubject.liveTest(initialState)

        assertEquals(false, mockDependency.createCalled.value)
    }

    @Test
    fun created_is_invoked_upon_request_in_live_test() = runTest {
        val mockDependency = FakeDependency()
        val testSubject = GeneralTestMiddleware(this, mockDependency)

        testSubject.liveTest(initialState = initialState).runOnCreate()

        assertEventually {
            assertEquals(true, mockDependency.createCalled.value)
        }
    }

    @Test
    fun first_intent_is_isolated_by_default() = runTest {
        val testSubject = GeneralTestMiddleware(this)
        val testContainerHost = testSubject.test(initialState)

        testContainerHost.testIntent { something() }

        assertEquals(true, (testSubject.dependency as FakeDependency).something1Called.value)
        assertEquals(false, testSubject.dependency.something2Called.value)
    }

    @Test
    fun initial_state_can_be_omitted_from_test() = runTest {
        val testSubject = GeneralTestMiddleware(this)
        val testContainerHost = testSubject.test()

        assertContentEquals(listOf(initialState), testContainerHost.stateObserver.values)
        testContainerHost.assert(initialState)
    }

    private inner class GeneralTestMiddleware(scope: TestScope, val dependency: BogusDependency = FakeDependency()) :
        ContainerHost<State, Nothing> {
        override val container = scope.backgroundScope.container<State, Nothing>(initialState) {
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
