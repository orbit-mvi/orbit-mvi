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

package com.babylon.orbit2

import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SideEffectTest {

    private val fixture = kotlinFixture()

    @Test
    fun `side effects are not multicast`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()

        val testSideEffectObserver1 = middleware.container.sideEffectFlow.test()
        val testSideEffectObserver2 = middleware.container.sideEffectFlow.test()
        val testSideEffectObserver3 = middleware.container.sideEffectFlow.test()

        middleware.someFlow(action)
        middleware.someFlow(action2)
        middleware.someFlow(action3)

        val timeout = 500L
        testSideEffectObserver1.awaitCount(3, timeout)
        testSideEffectObserver2.awaitCount(3, timeout)
        testSideEffectObserver3.awaitCount(3, timeout)

        assertThat(testSideEffectObserver1.values).doesNotContainSequence(action, action2, action3)
        assertThat(testSideEffectObserver2.values).doesNotContainSequence(action, action2, action3)
        assertThat(testSideEffectObserver3.values).doesNotContainSequence(action, action2, action3)
    }

    @Test
    fun `side effects are cached when there are no subscribers`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()

        middleware.someFlow(action)
        middleware.someFlow(action2)
        middleware.someFlow(action3)

        val testSideEffectObserver1 = middleware.container.sideEffectFlow.test()

        testSideEffectObserver1.awaitCount(3)

        assertThat(testSideEffectObserver1.values).containsExactly(action, action2, action3)
    }

    @Test
    fun `consumed side effects are not resent`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()
        val testSideEffectObserver1 = middleware.container.sideEffectFlow.test()

        middleware.someFlow(action)
        middleware.someFlow(action2)
        middleware.someFlow(action3)
        testSideEffectObserver1.awaitCount(3)
        testSideEffectObserver1.close()

        val testSideEffectObserver2 = middleware.container.sideEffectFlow.test()

        testSideEffectObserver1.awaitCount(3, 10L)

        assertThat(testSideEffectObserver2.values).isEmpty()
    }

    @Test
    fun `only new side effects are emitted when resubscribing`() {
        val action = fixture<Int>()
        val middleware = Middleware()

        val testSideEffectObserver1 = middleware.container.sideEffectFlow.test()

        middleware.someFlow(action)

        testSideEffectObserver1.awaitCount(1)
        testSideEffectObserver1.close()

        GlobalScope.launch {
            repeat(1000) {
                middleware.someFlow(it)
            }
        }

        Thread.sleep(200)

        val testSideEffectObserver2 = middleware.container.sideEffectFlow.test()
        testSideEffectObserver2.awaitCount(1000)

        assertThat(testSideEffectObserver1.values).containsExactly(action)
        assertThat(testSideEffectObserver2.values).containsExactlyElementsOf((0..999).toList())
    }

    private class Middleware : ContainerHost<Unit, Int> {
        override val container: Container<Unit, Int> = CoroutineScope(Dispatchers.Unconfined).container(Unit)

        fun someFlow(action: Int) = orbit {
            sideEffect {
                post(action)
            }
        }
    }
}
