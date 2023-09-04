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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.coroutines.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class InfiniteFlowTest {

    @Test
    fun `infinite flow can be tested with delay skipping`() = runTest {
        InfiniteFlowMiddleware(this).test(this) {
            expectInitialState()

            containerHost.incrementForever()

            // Assert the first three states
            assertEquals(listOf(42, 43), awaitState())
            assertEquals(listOf(42, 43, 44), awaitState())
            assertEquals(listOf(42, 43, 44, 45), awaitState())

            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `infinite flow can be tested without delay skipping`() = runTest {
        val scope = TestScope()

        InfiniteFlowMiddleware(this).test(scope) {
            expectInitialState()

            val job = containerHost.incrementForever()

            // Assert the first three states
            scope.advanceTimeBy(3001)
            assertEquals(listOf(42, 43), awaitState())
            scope.advanceTimeBy(3001)
            assertEquals(listOf(42, 43, 44), awaitState())
            scope.advanceTimeBy(3001)
            assertEquals(listOf(42, 43, 44, 45), awaitState())

            job.cancel()
            scope.advanceTimeBy(1)
        }
    }

//    org.orbitmvi.orbit.SuspendingStrategyDispatchTest[jvm] > suspending test maintains test dispatcher through runTest[jvm] PASSED

    private inner class InfiniteFlowMiddleware(testScope: TestScope) : ContainerHost<List<Int>, Nothing> {
        override val container: Container<List<Int>, Nothing> = testScope.backgroundScope.container(listOf(42))

        fun incrementForever() = intent {
            while (coroutineContext.isActive) {
                delay(3000)
                reduce { state + (state.last() + 1) }
            }
        }
    }
}
