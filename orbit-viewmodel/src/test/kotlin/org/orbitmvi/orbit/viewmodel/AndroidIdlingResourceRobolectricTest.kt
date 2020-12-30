package org.orbitmvi.orbit.viewmodel

import androidx.test.espresso.Espresso
import androidx.test.espresso.idling.CountingIdlingResource
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.container
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class AndroidIdlingResourceRobolectricTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @After
    fun after() {
        scope.cancel()
    }

    @Test
    fun `idling resources have unique names`() {
        scope.container<TestState, Int>(
            initialState = TestState(0),
            settings = Container.Settings(idlingRegistry = AndroidIdlingResource())
        )

        scope.container<TestState, Int>(
            initialState = TestState(0),
            settings = Container.Settings(idlingRegistry = AndroidIdlingResource())
        )

        forceIdlingResourceSync()

        @Suppress("DEPRECATION")
        Espresso.getIdlingResources().size.shouldBe(2)
    }

    /**
     * Force IdlingRegistry to be synced into Espresso.baseRegistry
     */
    private fun forceIdlingResourceSync() {
        CountingIdlingResource("bob").apply {
            @Suppress("DEPRECATION")
            Espresso.registerIdlingResources(this)
            @Suppress("DEPRECATION")
            Espresso.unregisterIdlingResources(this)
        }
    }

    data class TestState(val value: Int)
}
