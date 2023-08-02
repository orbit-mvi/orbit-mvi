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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class RepeatOnSubscriptionTest {
    private val initialState = State()

    @Test
    fun `test does not hang when using repeatOnSubscription`() = runTest {
        TestMiddleware(this).test(this, initialState = initialState) {
            expectInitialState()

            this.containerHost.callOnSubscription { 42 }

            assertEquals(State(42), awaitState())
        }
    }

    private inner class TestMiddleware(scope: TestScope) : ContainerHost<State, Nothing> {
        override val container = scope.backgroundScope.container<State, Nothing>(initialState)

        fun callOnSubscription(externalCall: suspend () -> Int) = intent {
            repeatOnSubscription {
                val result = externalCall()
                reduce { State(result) }
            }
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
