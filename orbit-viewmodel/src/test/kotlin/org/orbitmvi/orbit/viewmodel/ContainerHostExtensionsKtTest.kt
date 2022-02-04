package org.orbitmvi.orbit.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import kotlin.random.Random
import kotlin.test.assertEquals
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
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.internal.RealContainer

@ExperimentalCoroutinesApi
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
            settings = Container.Settings(),
            subscribedCounterOverride = testSubscribedCounter
        )
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

    @Test
    fun `starts successfully`() {
        containerHost.observe(mockLifecycleOwner, state = { })

        // Ensure there are no subscribers
        assertEquals(0, testSubscribedCounter.counter)

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)
    }

    @Test
    fun `stops successfully`() {
        containerHost.observe(mockLifecycleOwner, state = { })

        // Start and ensure there is one subscriber
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertEquals(1, testSubscribedCounter.counter)

        // Stop and ensure there are no subscribers
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertEquals(0, testSubscribedCounter.counter)
    }

    @Test
    fun `resubscribes state`() {
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
}
