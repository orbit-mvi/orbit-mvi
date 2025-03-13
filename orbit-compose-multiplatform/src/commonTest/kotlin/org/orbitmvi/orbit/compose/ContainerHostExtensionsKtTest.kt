/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.internal.RealContainer
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@ExperimentalCoroutinesApi
class ContainerHostExtensionsKtTest : RobolectricTest() {

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

    @BeforeTest
    fun beforeTest() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
    }

    @AfterTest
    fun afterTest() {
        Dispatchers.resetMain()
        scope.cancel()
    }

    private fun ComposeUiTest.initialiseContainerHost(block: @Composable ContainerHost<Int, Int>.() -> Unit) {
        setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides mockLifecycleOwner
            ) {
                block(containerHost)
            }
        }
    }

    @Test
    fun as_state_subscribes_on_start() = runComposeUiTest {

        initialiseContainerHost { collectAsState() }

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun as_state_unsubscribes_on_stop() = runComposeUiTest {
        initialiseContainerHost { collectAsState() }

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun as_state_resubscribes_when_restarted() = runComposeUiTest {
        initialiseContainerHost { collectAsState() }

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
    fun as_state_subscribes_on_custom_lifecycle() = runComposeUiTest {
        initialiseContainerHost { collectAsState(Lifecycle.State.RESUMED) }

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun as_state_unsubscribes_on_stop_with_custom_lifecycle() = runComposeUiTest {
        initialiseContainerHost { collectAsState(Lifecycle.State.RESUMED) }

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun as_state_resubscribes_when_restarted_on_custom_lifecycle() = runComposeUiTest {
        initialiseContainerHost { collectAsState(Lifecycle.State.RESUMED) }

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
    fun side_effect_subscribes_on_start() = runComposeUiTest {
        initialiseContainerHost { collectSideEffect { } }

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun side_effect_unsubscribes_on_stop() = runComposeUiTest {
        initialiseContainerHost { collectSideEffect { } }

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun side_effect_resubscribes_when_restarted() = runComposeUiTest {
        initialiseContainerHost { collectSideEffect { } }

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
    fun side_effect_subscribes_on_custom_lifecycle() = runComposeUiTest {
        initialiseContainerHost { collectSideEffect(Lifecycle.State.RESUMED) { } }

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun side_effect_unsubscribes_on_stop_with_custom_lifecycle() = runComposeUiTest {
        initialiseContainerHost { collectSideEffect(Lifecycle.State.RESUMED) { } }

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun side_effect_resubscribes_when_restarted_on_custom_lifecycle() = runComposeUiTest {
        initialiseContainerHost { collectSideEffect(Lifecycle.State.RESUMED) { } }

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
