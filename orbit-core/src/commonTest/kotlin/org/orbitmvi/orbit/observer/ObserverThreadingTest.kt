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

package org.orbitmvi.orbit.observer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.orbitContainer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the observer's exactness guarantees under concurrency: reported reductions are the
 * winning CAS transitions only, and the full event stream reconstructs the exact state history
 * even when reduction callbacks arrive out of order across threads.
 */
internal class ObserverThreadingTest {

    private val observer = RecordingObserver()

    @Test
    fun winning_cas_transitions_reported_exactly_under_contention() = runTest {
        val container = backgroundScope.orbitContainer<Int, Nothing>(
            initialState = 0,
            buildSettings = {
                eventObserver = observer
                intentLaunchingDispatcher = { Dispatchers.Default }
            }
        )

        repeat(INTENT_COUNT) {
            container.orbit {
                repeat(REDUCTIONS_PER_INTENT) {
                    reduce { it + 1 }
                }
            }
        }
        container.joinIntents()

        val expectedCount = INTENT_COUNT * REDUCTIONS_PER_INTENT
        assertEquals(expectedCount, container.stateFlow.value)

        val reductions = observer.eventsOf<RecordedEvent.Reduction>()
        assertEquals(expectedCount, reductions.size)
        // Winning transitions form an exact chain: every intermediate state appears exactly once
        // as an old state, and every reduction is an increment of the state it actually replaced.
        // Recording a lost CAS attempt would duplicate an old state and break the chain.
        assertEquals((0 until expectedCount).toSet(), reductions.map { it.oldState }.toSet())
        reductions.forEach { assertEquals(it.oldState as Int + 1, it.newState) }
    }

    @Test
    fun event_stream_reconstructs_exact_history_under_threading_stress() = runTest {
        val container = backgroundScope.orbitContainer<TestState, Nothing>(
            initialState = TestState(),
            buildSettings = { eventObserver = observer }
        )

        for (i in 0 until ITEM_COUNT) {
            launch {
                val value = (i % 3) + 1
                container.orbit {
                    reduce { it.copy(ids = it.ids + value) }
                }
            }
        }
        testScheduler.advanceUntilIdle()
        container.joinIntents()

        val reductions = observer.eventsOf<RecordedEvent.Reduction>()
        assertEquals(ITEM_COUNT, reductions.size)

        // Each reduction appends exactly one element, so state sizes identify chain positions
        val bySize = reductions.associateBy { (it.oldState as TestState).ids.size }
        assertEquals((0 until ITEM_COUNT).toSet(), bySize.keys)

        // The chain links exactly: each reported new state is the next reduction's old state
        for (i in 0 until ITEM_COUNT - 1) {
            assertEquals(bySize.getValue(i).newState, bySize.getValue(i + 1).oldState)
        }
        assertEquals(container.stateFlow.value, bySize.getValue(ITEM_COUNT - 1).newState)

        // Every dispatched intent completed
        val dispatchedIds = observer.eventsOf<RecordedEvent.Dispatched>().map { it.intentId }.toSet()
        val completedIds = observer.eventsOf<RecordedEvent.Completed>().map { it.intentId }.toSet()
        assertEquals(ITEM_COUNT, dispatchedIds.size)
        assertEquals(dispatchedIds, completedIds)
    }

    private data class TestState(val ids: List<Int> = emptyList())

    private companion object {
        const val INTENT_COUNT = 4
        const val REDUCTIONS_PER_INTENT = 250
        const val ITEM_COUNT = 1119
    }
}
