/*
 * Copyright 2022 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.internal.RealContainer
import kotlin.random.Random
import kotlin.test.assertEquals

class ContainerHostExtensionsKtTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mockLifecycleOwner = MockLifecycleOwner()

    private val testSubscribedCounter = TestSubscribedCounter()

    private val scope by lazy { CoroutineScope(Job()) }

    private val containerHost = object : ContainerHost<Int, Int> {
        override val container = RealContainer<Int, Int>(
            initialState = Random.nextInt(),
            parentScope = scope,
            settings = RealSettings(),
            subscribedCounterOverride = testSubscribedCounter
        )
    }

    @Before
    @ExperimentalCoroutinesApi
    fun beforeTest() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
    }

    @After
    @ExperimentalCoroutinesApi
    fun afterTest() {
        Dispatchers.resetMain()
        scope.cancel()
    }

    @Test
    fun `state subscribes on start`() {
        containerHost.observe(mockLifecycleOwner, state = { })

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun `state unsubscribes on stop`() {
        containerHost.observe(mockLifecycleOwner, state = { })

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun `state resubscribes when restarted`() {
        containerHost.observe(mockLifecycleOwner, state = { })

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)

        // Re-start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun `state subscribes on custom lifecycle`() {
        containerHost.observe(mockLifecycleOwner, lifecycleState = Lifecycle.State.RESUMED, state = { })

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun `state unsubscribes on stop with custom lifecycle`() {
        containerHost.observe(mockLifecycleOwner, lifecycleState = Lifecycle.State.RESUMED, state = { })

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun `state resubscribes when restarted on custom lifecycle`() {
        containerHost.observe(mockLifecycleOwner, lifecycleState = Lifecycle.State.RESUMED, state = { })

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)

        // Re-start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun `side effect subscribes on start`() {
        containerHost.observe(mockLifecycleOwner, sideEffect = { })

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun `side effect unsubscribes on stop`() {
        containerHost.observe(mockLifecycleOwner, sideEffect = { })

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun `side effect resubscribes when restarted`() {
        containerHost.observe(mockLifecycleOwner, sideEffect = { })

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)

        // Re-start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun `side effect subscribes on custom lifecycle`() {
        containerHost.observe(mockLifecycleOwner, lifecycleState = Lifecycle.State.RESUMED, sideEffect = { })

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun `side effect unsubscribes on stop with custom lifecycle`() {
        containerHost.observe(mockLifecycleOwner, lifecycleState = Lifecycle.State.RESUMED, sideEffect = { })

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun `side effect resubscribes when restarted on custom lifecycle`() {
        containerHost.observe(mockLifecycleOwner, lifecycleState = Lifecycle.State.RESUMED, sideEffect = { })

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)

        // Re-start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)
    }
}
