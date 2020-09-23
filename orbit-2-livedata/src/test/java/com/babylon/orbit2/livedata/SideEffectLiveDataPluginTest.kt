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

package com.babylon.orbit2.livedata

import androidx.lifecycle.Lifecycle
import com.appmattus.kotlinfixture.kotlinFixture
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.sideEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Suppress("DEPRECATION")
@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class SideEffectLiveDataPluginTest {
    private val fixture = kotlinFixture()
    private val mockLifecycleOwner = MockLifecycleOwner().also {
        it.dispatchEvent(Lifecycle.Event.ON_CREATE)
        it.dispatchEvent(Lifecycle.Event.ON_START)
    }

    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Test
    fun `side effects are not multicast to observers using separate livedatas`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()
        val testSideEffectObserver1 = middleware.container.sideEffect.test(mockLifecycleOwner)
        val testSideEffectObserver2 = middleware.container.sideEffect.test(mockLifecycleOwner)
        val testSideEffectObserver3 = middleware.container.sideEffect.test(mockLifecycleOwner)

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
    fun `side effects are not multicast to all current observers using a single livedata`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()
        val liveData = middleware.container.sideEffect
        val testSideEffectObserver1 =
            liveData.test(mockLifecycleOwner)
        val testSideEffectObserver2 =
            liveData.test(mockLifecycleOwner)
        val testSideEffectObserver3 =
            liveData.test(mockLifecycleOwner)

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

        val testSideEffectObserver1 = middleware.container.sideEffect.test(mockLifecycleOwner)

        testSideEffectObserver1.awaitCount(3)

        assertThat(testSideEffectObserver1.values).containsExactly(action, action2, action3)
    }

    @Test
    fun `observer is unsubscribed after onDestroy`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()
        val testSideEffectObserver1 = middleware.container.sideEffect.test(mockLifecycleOwner)

        middleware.someFlow(action)
        testSideEffectObserver1.awaitCount(1)

        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_DESTROY)

        middleware.someFlow(action2)
        middleware.someFlow(action3)

        testSideEffectObserver1.awaitCount(3, 500L)
        assertThat(testSideEffectObserver1.values).containsExactly(action)
    }

    @Test
    fun `new subscribers do not get updates if there is already a sub`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()
        val liveData = middleware.container.sideEffect
        val testSideEffectObserver1 = liveData.test(mockLifecycleOwner)

        middleware.someFlow(action)
        middleware.someFlow(action2)
        middleware.someFlow(action3)

        testSideEffectObserver1.awaitCount(3)

        val testSideEffectObserver2 = liveData.test(mockLifecycleOwner)

        assertThat(testSideEffectObserver1.values).containsExactly(action, action2, action3)
        assertThat(testSideEffectObserver2.values).isEmpty()
    }

    @Test
    fun `side effects are not conflated`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()
        val testSideEffectObserver = middleware.container.sideEffect.test(mockLifecycleOwner)

        middleware.someFlow(action)
        middleware.someFlow(action2)
        middleware.someFlow(action3)

        testSideEffectObserver.awaitCount(3)

        assertThat(testSideEffectObserver.values).containsExactly(action, action2, action3)
    }

    @Test
    fun `consecutive equal objects are emitted properly`() {
        val action = fixture<Int>()
        val middleware = Middleware()
        val testSideEffectObserver = middleware.container.sideEffect.test(mockLifecycleOwner)

        middleware.someFlow(action)
        middleware.someFlow(action)
        middleware.someFlow(action)

        testSideEffectObserver.awaitCount(3)

        assertThat(testSideEffectObserver.values).containsExactly(action, action, action)
    }

    @Test
    fun `only new side effects are emitted when resubscribing to the same live data`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()
        val liveData = middleware.container.sideEffect
        val testSideEffectObserver1 = liveData.test(mockLifecycleOwner)

        middleware.someFlow(action)

        testSideEffectObserver1.awaitCount(1)
        testSideEffectObserver1.close()
        testSideEffectObserver1.awaitNoActiveObservers()

        middleware.someFlow(action2)
        middleware.someFlow(action3)

        val testSideEffectObserver2 = liveData.test(mockLifecycleOwner)
        testSideEffectObserver2.awaitCount(2)

        assertThat(testSideEffectObserver1.values).containsExactly(action)
        assertThat(testSideEffectObserver2.values).containsExactly(action2, action3)
    }

    @Test
    fun `only new side effects are emitted when resubscribing to different live datas`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware()
        val testSideEffectObserver1 = middleware.container.sideEffect.test(mockLifecycleOwner)

        middleware.someFlow(action)

        testSideEffectObserver1.awaitCount(1)
        testSideEffectObserver1.close()
        testSideEffectObserver1.awaitNoActiveObservers()

        middleware.someFlow(action2)
        middleware.someFlow(action3)

        val testSideEffectObserver2 =
            middleware.container.sideEffect.test(mockLifecycleOwner)
        testSideEffectObserver2.awaitCount(2)

        assertThat(testSideEffectObserver1.values).containsExactly(action)
        assertThat(testSideEffectObserver2.values).containsExactly(action2, action3)
    }

    @Test
    fun `side effects are delivered in two different subscriptions`() {
        val action = fixture<Int>()
        val middleware = Middleware()
        val testSideEffectObserver = middleware.container.sideEffect.test(mockLifecycleOwner)

        middleware.someFlow(action)
        testSideEffectObserver.awaitCount(1)

        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        testSideEffectObserver.close()
        testSideEffectObserver.awaitNoActiveObservers()

        assertThat(testSideEffectObserver.values).containsExactly(action)

        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        val testSideEffectObserver2 = middleware.container.sideEffect.test(mockLifecycleOwner)

        middleware.someFlow(action)
        middleware.someFlow(action + 1)

        testSideEffectObserver2.awaitCount(2)

        assertThat(testSideEffectObserver2.values).containsExactly(action, action + 1)
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
