/*
 * Copyright 2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.internal.repeatonsubscription

import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Duration.Companion.milliseconds

internal class RepeatOnSubscriptionTest {
    private val initialState = State()

    @Test
    fun block_is_not_executed_when_there_is_no_subscription_to_the_ref_count_state_flow() = runTest {
        val testSubject = TestMiddleware(this)

        testSubject.container.stateFlow.test(timeout = 500.milliseconds) {
            assertEquals(initialState, awaitItem())

            val intentJob = testSubject.updateState { 42 }

            assertFails { awaitItem() }
            intentJob.cancel()
        }
    }

    @Test
    fun block_is_executed_when_there_is_a_subscription_to_the_state_flow() = runTest {
        val testSubject = TestMiddleware(this)

        testSubject.container.refCountStateFlow.test {
            assertEquals(initialState, awaitItem())

            val intentJob = testSubject.updateState { 42 }

            assertEquals(State(42), awaitItem())
            intentJob.cancel()
        }
    }

    @Test
    fun block_is_not_executed_when_there_is_no_subscription_to_the_ref_count_side_effect_flow() = runTest {
        val testSubject = TestMiddleware(this)

        testSubject.container.sideEffectFlow.test(timeout = 500.milliseconds) {
            val intentJob = testSubject.updateSideEffect { 42 }

            assertFails { awaitItem() }
            intentJob.cancel()
        }
    }

    @Test
    fun block_is_executed_when_there_is_a_subscription_to_the_ref_count_side_effect_flow() = runTest {
        val testSubject = TestMiddleware(this)

        testSubject.container.refCountSideEffectFlow.test {
            val intentJob = testSubject.updateSideEffect { 42 }

            assertEquals(42, awaitItem())
            intentJob.cancel()
        }
    }

    private inner class TestMiddleware(testScope: TestScope) : ContainerHost<State, Int> {
        override val container = testScope.backgroundScope.container<State, Int>(initialState)

        fun updateState(externalCall: suspend () -> Int) = intent {
            repeatOnSubscription {
                val result = externalCall()
                reduce { State(result) }
            }
        }

        fun updateSideEffect(externalCall: suspend () -> Int) = intent {
            repeatOnSubscription {
                val result = externalCall()
                postSideEffect(result)
            }
        }
    }

    private data class State(val count: Int = Random.nextInt())
}
