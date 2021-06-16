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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class DispatcherTest {
    companion object {
        const val TIMEOUT = 1000L
    }

    private val initialState = State()

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun `default blocking test works`() {
        val testSubject = StateTestMiddleware().test(initialState = initialState)
        val action = Random.nextInt()

        // This should block
        testSubject.somethingInBackground(action)

        testSubject.assert(initialState) {
            states(
                { copy(count = action) }
            )
        }
    }

    @Test
    fun `run blocking test works`() {
        val testCoroutineDispatcher = TestCoroutineDispatcher()
        val testSubject = StateTestMiddleware().apply {
            test(
                initialState = initialState,
                settings = container.settings.copy(
                    orbitDispatcher = testCoroutineDispatcher,
                    backgroundDispatcher = testCoroutineDispatcher
                )
            )
        }
        runBlockingTest(testCoroutineDispatcher) {
            val action = Random.nextInt()

            // This should block
            testSubject.somethingInBackground(action)
            testCoroutineDispatcher.advanceTimeBy(1010)

            testSubject.assert(initialState) {
                states(
                    { copy(count = action) }
                )
            }
        }
    }

    private inner class StateTestMiddleware :
        ContainerHost<State, Nothing> {
        override val container = scope.container<State, Nothing>(initialState)

        fun somethingInBackground(action: Int): Unit = intent {
//            withContext(Dispatchers.Default) {
                delay(1000)
                reduce {
                    State(count = action)
                }
//            }
        }
    }

    private data class State(val count: Int = Random.nextInt())
}