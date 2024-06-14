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

package org.orbitmvi.orbit.compose

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.ContainerHostWithExtState
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.internal.RealContainer
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class ComposeExtStateTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockLifecycleOwner = MockLifecycleOwner()

    private val testSubscribedCounter = TestSubscribedCounter()

    private val scope by lazy { CoroutineScope(Job()) }

    private val containerHost = object : ContainerHost<Int, Int> {
        override val container = RealContainer<Int, Int>(
            initialState = Random.nextInt(),
            settings = RealSettings(
                subscribedCounter = testSubscribedCounter,
                parentScope = scope,
            ),
        )
    }

    private val containerHostWithExtState = object : ContainerHostWithExtState<Int, Int, ExtState> {
        override val container = containerHost.container.withExtState { ExtState(it) }
    }

    @Before
    fun beforeTest() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
    }

    @After
    fun afterTest() {
        Dispatchers.resetMain()
        scope.cancel()
    }

    private fun initialiseContainerHostWithExtState(block: @Composable ContainerHostWithExtState<Int, Int, ExtState>.() -> State<ExtState>) {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides mockLifecycleOwner
            ) {
                val state = containerHostWithExtState.block()

                println(state)
            }
        }
    }

    @Test
    fun as_state_subscribes_on_start() {
        initialiseContainerHostWithExtState { collectExtState() }

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun as_state_unsubscribes_on_stop() {
        initialiseContainerHostWithExtState { collectExtState() }

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun as_state_resubscribes_when_restarted() {
        initialiseContainerHostWithExtState { collectExtState() }

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
    fun as_state_subscribes_on_custom_lifecycle() {
        initialiseContainerHostWithExtState { collectExtState(Lifecycle.State.RESUMED) }

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun as_state_unsubscribes_on_stop_with_custom_lifecycle() {
        initialiseContainerHostWithExtState { collectExtState(Lifecycle.State.RESUMED) }

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun as_state_resubscribes_when_restarted_on_custom_lifecycle() = runTest {
        initialiseContainerHostWithExtState { collectExtState(Lifecycle.State.RESUMED) }

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

    @JvmInline
    value class ExtState(val value: Int)
}
