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

import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withTimeout
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription

@ExperimentalCoroutinesApi
internal class RepeatOnSubscriptionTest {
    private val initialState = State()
    private val scope by lazy { CoroutineScope(Job()) }

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun `test does not hang when using repeatOnSubscription`() = runBlocking {
        val testSubject = TestMiddleware().test(initialState = initialState, settings = Container.Settings())

        withTimeout(1000L) {
            testSubject.testIntent {
                callOnSubscription { 42 }
            }
        }

        testSubject.assert(initialState) {
            states(
                { State(42) }
            )
        }
    }

    private inner class TestMiddleware : ContainerHost<State, Nothing> {
        override val container = scope.container<State, Nothing>(initialState)

        fun callOnSubscription(externalCall: suspend () -> Int) = intent {
            repeatOnSubscription {
                val result = externalCall()
                reduce { State(result) }
            }
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
