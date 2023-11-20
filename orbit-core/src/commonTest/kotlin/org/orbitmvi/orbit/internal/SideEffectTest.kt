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

package org.orbitmvi.orbit.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test.assertContainExactly
import org.orbitmvi.orbit.test.assertNotContainExactly
import org.orbitmvi.orbit.testFlowObserver
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class SideEffectTest {

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun side_effects_are_emitted_in_order() = runTest {
        val container = scope.container<Unit, Int>(Unit)

        val testSideEffectObserver1 = container.sideEffectFlow.testFlowObserver()

        repeat(1000) {
            container.someFlow(it)
        }

        testSideEffectObserver1.awaitCount(1000)

        testSideEffectObserver1.values.assertContainExactly((0..999).toList())
    }

    @Test
    fun side_effects_are_not_multicast() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = scope.container<Unit, Int>(Unit)

        val testSideEffectObserver1 = container.sideEffectFlow.testFlowObserver()
        val testSideEffectObserver2 = container.sideEffectFlow.testFlowObserver()
        val testSideEffectObserver3 = container.sideEffectFlow.testFlowObserver()

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)

        val timeout = 500L
        testSideEffectObserver1.awaitCount(3, timeout)
        testSideEffectObserver2.awaitCount(3, timeout)
        testSideEffectObserver3.awaitCount(3, timeout)

        testSideEffectObserver1.values.assertNotContainExactly(action, action2, action3)
        testSideEffectObserver2.values.assertNotContainExactly(action, action2, action3)
        testSideEffectObserver3.values.assertNotContainExactly(action, action2, action3)
    }

    @Test
    fun side_effects_are_cached_when_there_are_no_subscribers() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = scope.container<Unit, Int>(Unit)

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)

        val testSideEffectObserver1 = container.sideEffectFlow.testFlowObserver()

        testSideEffectObserver1.awaitCount(3)

        testSideEffectObserver1.values.assertContainExactly(action, action2, action3)
    }

    @Test
    fun consumed_side_effects_are_not_resent() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = scope.container<Unit, Int>(Unit)
        val testSideEffectObserver1 = container.sideEffectFlow.testFlowObserver()

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)
        testSideEffectObserver1.awaitCount(3)
        testSideEffectObserver1.close()

        val testSideEffectObserver2 = container.sideEffectFlow.testFlowObserver()

        testSideEffectObserver1.awaitCount(3, 10L)

        assertEquals(0, testSideEffectObserver2.values.size, "should be empty")
    }

    @Test
    fun only_new_side_effects_are_emitted_when_resubscribing() = runTest {
        val action = Random.nextInt()
        val container = scope.container<Unit, Int>(Unit)

        val testSideEffectObserver1 = container.sideEffectFlow.testFlowObserver()

        container.someFlow(action)

        testSideEffectObserver1.awaitCount(1)
        testSideEffectObserver1.close()

        coroutineScope {
            launch {
                repeat(1000) {
                    container.someFlow(it)
                }
            }
        }

        val testSideEffectObserver2 = container.sideEffectFlow.testFlowObserver()
        testSideEffectObserver2.awaitCount(1000)

        testSideEffectObserver1.values.assertContainExactly(action)
        testSideEffectObserver2.values.assertContainExactly((0..999).toList())
    }

    private suspend fun Container<Unit, Int>.someFlow(action: Int) = orbit {
        postSideEffect(action)
    }
}
