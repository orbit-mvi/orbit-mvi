/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.internal

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.SideEffectMode
import org.orbitmvi.orbit.orbitContainer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class BroadcastSideEffectTest {

    @Test
    fun side_effects_are_emitted_in_order() = runTest {
        val container = createContainer()

        container.sideEffectFlow.test {
            repeat(1000) {
                container.someFlow(it)
            }

            repeat(1000) {
                assertEquals(it, awaitItem())
            }
        }
    }

    @Test
    fun side_effects_are_cached_when_there_are_no_subscribers() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = createContainer()

        joinAll(
            container.someFlow(action),
            container.someFlow(action2),
            container.someFlow(action3)
        )

        container.sideEffectFlow.test {
            assertEquals(action, awaitItem())
            assertEquals(action2, awaitItem())
            assertEquals(action3, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun cached_side_effects_are_delivered_to_all_consumers() = runTest {
        val action = Random.nextInt()
        val action2 = Random.nextInt()
        val action3 = Random.nextInt()
        val container = createContainer()

        joinAll(
            container.someFlow(action),
            container.someFlow(action2),
            container.someFlow(action3)
        )

        // Both consumers should receive all cached side effects
        launch {
            container.sideEffectFlow.test {
                assertEquals(action, awaitItem())
                assertEquals(action2, awaitItem())
                assertEquals(action3, awaitItem())
                ensureAllEventsConsumed()
                cancel()
            }
        }

        launch {
            container.sideEffectFlow.test {
                assertEquals(action, awaitItem())
                assertEquals(action2, awaitItem())
                assertEquals(action3, awaitItem())
                ensureAllEventsConsumed()
                cancel()
            }
        }
    }

    @Test
    fun live_side_effects_are_delivered_to_all_consumers() = runTest {
        val container = createContainer()
        val received1 = mutableListOf<Int>()
        val received2 = mutableListOf<Int>()

        val job1 = launch {
            container.sideEffectFlow.collect { received1.add(it) }
        }
        val job2 = launch {
            container.sideEffectFlow.collect { received2.add(it) }
        }

        // Let collectors start
        advanceUntilIdle()

        joinAll(
            container.someFlow(1),
            container.someFlow(2),
            container.someFlow(3)
        )

        advanceUntilIdle()

        assertEquals(listOf(1, 2, 3), received1)
        assertEquals(listOf(1, 2, 3), received2)

        job1.cancel()
        job2.cancel()
    }

    @Test
    fun live_side_effects_are_not_replayed_to_a_later_subscriber() = runTest {
        val action = Random.nextInt()
        val container = createContainer()

        // Emitted while a subscriber is active, so it is a live effect and must not be cached for replay.
        container.sideEffectFlow.test {
            container.someFlow(action)
            assertEquals(action, awaitItem())
            ensureAllEventsConsumed()
            cancel()
        }

        // A later subscriber must not receive the already-delivered live effect.
        container.sideEffectFlow.test {
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun live_side_effect_is_not_replayed_after_resubscribe() = runTest {
        val action = Random.nextInt()
        val container = createContainer()

        // First subscription receives the live effect (e.g. a navigation side effect).
        container.refCountSideEffectFlow.test {
            container.someFlow(action)
            assertEquals(action, awaitItem())
            ensureAllEventsConsumed()
            cancel()
        }

        // Re-subscribing (e.g. navigating back to the screen) must not replay the navigation side effect.
        container.refCountSideEffectFlow.test {
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun replay_cache_is_cleared_after_ref_count_subscription() = runTest {
        val action = Random.nextInt()
        val container = createContainer()

        container.someFlow(action)

        // Subscribe via refCountSideEffectFlow to trigger the subscribed counter
        container.refCountSideEffectFlow.test {
            assertEquals(action, awaitItem())
            cancel()
        }

        // The replay clear runs on Dispatchers.Default (not the test dispatcher),
        // so we need to wait real wall-clock time for it to complete.
        withContext(Dispatchers.Default) { delay(300) }

        // After replay cache clear, new subscriber should not see stale items
        container.sideEffectFlow.test {
            expectNoEvents()
            cancel()
        }
    }

    @Test
    fun caching_suspends_when_the_cache_is_full() = runTest {
        val container: OrbitContainer<Unit, Unit, Int> = backgroundScope.orbitContainer(
            initialState = Unit,
            buildSettings = {
                sideEffectMode = SideEffectMode.BROADCAST
                sideEffectBufferSize = 1
            }
        )

        // Fills the single cache slot (no subscriber, so it is cached).
        container.someFlow(1).join()

        // No free slot and no subscriber, so the producing intent must suspend.
        val blocked = container.someFlow(2)
        withContext(Dispatchers.Default) { delay(200) }
        assertTrue(blocked.isActive, "producer should suspend while the cache is full")

        // A subscriber connecting receives the cached effect and the suspended overflow effect (broadcast live),
        // allowing the producer to complete.
        container.refCountSideEffectFlow.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            cancel()
        }
        withContext(Dispatchers.Default) { delay(300) }
        blocked.join()
        assertFalse(blocked.isActive)
    }

    @Test
    fun overflow_is_broadcast_live_not_recached() = runTest {
        val container: OrbitContainer<Unit, Unit, Int> = backgroundScope.orbitContainer(
            initialState = Unit,
            buildSettings = {
                sideEffectMode = SideEffectMode.BROADCAST
                sideEffectBufferSize = 1
            }
        )

        // Fill the single cache slot, then overflow with a second effect while nothing is connected.
        container.someFlow(1).join()
        val overflow = container.someFlow(2)
        withContext(Dispatchers.Default) { delay(100) }
        assertTrue(overflow.isActive, "overflow producer should suspend while the cache is full")

        // A subscriber connecting receives the cached effect (1) and the overflow effect (2) broadcast live.
        container.refCountSideEffectFlow.test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            cancel()
        }
        withContext(Dispatchers.Default) { delay(300) }
        overflow.join()

        // The overflow effect must NOT have been re-cached: a later subscriber sees nothing.
        container.refCountSideEffectFlow.test {
            expectNoEvents()
            cancel()
        }
    }

    private fun TestScope.createContainer(): OrbitContainer<Unit, Unit, Int> =
        backgroundScope.orbitContainer(
            initialState = Unit,
            buildSettings = { sideEffectMode = SideEffectMode.BROADCAST }
        )

    private suspend fun OrbitContainer<Unit, Unit, Int>.someFlow(action: Int) = orbit {
        postSideEffect(action)
    }
}
