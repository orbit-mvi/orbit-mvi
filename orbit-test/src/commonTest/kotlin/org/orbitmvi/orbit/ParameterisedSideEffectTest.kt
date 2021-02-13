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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.orbitmvi.orbit.syntax.strict.orbit
import org.orbitmvi.orbit.syntax.strict.sideEffect
import kotlin.random.Random
import kotlin.test.AfterTest

@ExperimentalCoroutinesApi
internal class ParameterisedSideEffectTest(blocking: Boolean) {
    companion object {
        const val TIMEOUT = 1000L
    }

    private val initialState = State()
    private val scope = CoroutineScope(Job())
    private val testSubject = SideEffectTestMiddleware().test(
        initialState = initialState,
        isolateFlow = false,
        blocking = blocking
    )

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    fun `succeeds if posted side effects match expected side effects`() {
        val sideEffects = List(Random.nextInt(1, 5)) { Random.nextInt() }

        sideEffects.forEach { testSubject.something(it) }

        testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
            postedSideEffects(sideEffects)
        }
    }

    fun `fails if posted side effects do not match expected side effects`() {
        val sideEffects = List(Random.nextInt(1, 5)) { Random.nextInt() }
        val sideEffects2 = List(Random.nextInt(1, 5)) { Random.nextInt() }

        sideEffects.forEach { testSubject.something(it) }

        val throwable = shouldThrow<AssertionError> {
            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                postedSideEffects(sideEffects2)
            }
        }

        throwable.message.shouldContain(
            "<${Regex.escape(sideEffects2.toString())}>[^<]*<${Regex.escape(sideEffects.toString())}>".toRegex()
        )
    }

    private inner class SideEffectTestMiddleware :
        ContainerHost<State, Int> {
        override val container = scope.container<State, Int>(initialState)

        fun something(action: Int): Unit = orbit {
            sideEffect {
                post(action)
            }
                .sideEffect { somethingElse(action.toString()) }
        }

        fun somethingElse(action: String) = orbit {
            sideEffect {
                println(action)
            }
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
