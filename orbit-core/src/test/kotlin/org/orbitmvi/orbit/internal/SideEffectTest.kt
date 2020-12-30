/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.orbitmvi.orbit.internal

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContainExactly
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
internal class SideEffectTest {

    private val scope = TestCoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cleanupTestCoroutines()
        scope.cancel()
    }

    @Test
    fun `side effects are emitted in order`() {
        val container = scope.container<Unit, Int>(Unit)

        val testSideEffectObserver1 = container.sideEffectFlow.test()

        repeat(1000) {
            container.someFlow(it)
        }

        testSideEffectObserver1.awaitCount(1000)

        testSideEffectObserver1.values.shouldContainExactly((0..999).toList())
    }

    @Test
    fun `side effects are not multicast`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = scope.container<Unit, Int>(Unit)

        val testSideEffectObserver1 = container.sideEffectFlow.test()
        val testSideEffectObserver2 = container.sideEffectFlow.test()
        val testSideEffectObserver3 = container.sideEffectFlow.test()

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)

        val timeout = 500L
        testSideEffectObserver1.awaitCount(3, timeout)
        testSideEffectObserver2.awaitCount(3, timeout)
        testSideEffectObserver3.awaitCount(3, timeout)

        testSideEffectObserver1.values.shouldNotContainExactly(action, action2, action3)
        testSideEffectObserver2.values.shouldNotContainExactly(action, action2, action3)
        testSideEffectObserver3.values.shouldNotContainExactly(action, action2, action3)
    }

    @Test
    fun `side effects are cached when there are no subscribers`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = scope.container<Unit, Int>(Unit)

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)

        val testSideEffectObserver1 = container.sideEffectFlow.test()

        testSideEffectObserver1.awaitCount(3)

        testSideEffectObserver1.values.shouldContainExactly(action, action2, action3)
    }

    @Test
    fun `consumed side effects are not resent`() {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = scope.container<Unit, Int>(Unit)
        val testSideEffectObserver1 = container.sideEffectFlow.test()

        container.someFlow(action)
        container.someFlow(action2)
        container.someFlow(action3)
        testSideEffectObserver1.awaitCount(3)
        testSideEffectObserver1.close()

        val testSideEffectObserver2 = container.sideEffectFlow.test()

        testSideEffectObserver1.awaitCount(3, 10L)

        testSideEffectObserver2.values.shouldBeEmpty()
    }

    @Test
    fun `only new side effects are emitted when resubscribing`() {
        val action = Random.nextInt()
        val container = scope.container<Unit, Int>(Unit)

        val testSideEffectObserver1 = container.sideEffectFlow.test()

        container.someFlow(action)

        testSideEffectObserver1.awaitCount(1)
        testSideEffectObserver1.close()

        GlobalScope.launch {
            repeat(1000) {
                container.someFlow(it)
            }
        }

        val testSideEffectObserver2 = container.sideEffectFlow.test()
        testSideEffectObserver2.awaitCount(1000)

        testSideEffectObserver1.values.shouldContainExactly(action)
        testSideEffectObserver2.values.shouldContainExactly((0..999).toList())
    }

    private fun Container<Unit, Int>.someFlow(action: Int) = orbit {
        postSideEffect(action)
    }
}
