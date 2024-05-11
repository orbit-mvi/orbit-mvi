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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import kotlin.random.Random
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

@Suppress("DEPRECATION")
@ExperimentalCoroutinesApi
internal class ParameterisedSideEffectTest(private val blocking: Boolean) {
    companion object {
        const val TIMEOUT = 2000L
    }

    private val initialState = State()

    fun `succeeds if posted side effects match expected side effects`() = runTest {
        val testSubject = testSubject(this)
        val sideEffects = List(Random.nextInt(1, 5)) { Random.nextInt() }

        sideEffects.forEach { testSubject.call { something(it) } }

        testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
            postedSideEffects(sideEffects)
        }
    }

    fun `fails if posted side effects do not match expected side effects`() = runTest {
        val testSubject = testSubject(this)
        val sideEffects = List(Random.nextInt(1, 5)) { Random.nextInt() }
        val sideEffects2 = List(Random.nextInt(1, 5)) { Random.nextInt() }

        sideEffects.forEach { testSubject.call { something(it) } }

        // Ensure all events are sent
        testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
            postedSideEffects(sideEffects)
        }

        val throwable = assertFailsWith<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                postedSideEffects(sideEffects2)
            }
        }

        assertContains(throwable.message.orEmpty(), sideEffects2.toString())
        assertContains(throwable.message.orEmpty(), sideEffects.toString())
    }

    private fun testSubject(scope: TestScope) = if (blocking) {
        SideEffectTestMiddleware(scope).test(
            initialState = initialState
        )
    } else {
        SideEffectTestMiddleware(scope).liveTest(initialState)
    }

    private inner class SideEffectTestMiddleware(scope: TestScope) :
        ContainerHost<State, Int> {
        override val container = scope.backgroundScope.container<State, Int>(initialState)

        fun something(action: Int) = intent {
            postSideEffect(action)
            somethingElse(action.toString())
        }

        fun somethingElse(action: String) = intent {
            println(action)
        }
    }

    private data class State(val count: Int = Random.nextInt())

    private suspend fun <STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>> TestContainerHost<STATE, SIDE_EFFECT, T>.call(
        block: T.() -> Unit
    ) {
        when (this) {
            is SuspendingTestContainerHost -> testIntent { block() }
            is RegularTestContainerHost -> testIntent { block() }
        }
    }
}
