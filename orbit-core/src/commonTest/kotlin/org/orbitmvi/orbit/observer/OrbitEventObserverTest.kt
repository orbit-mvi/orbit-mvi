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

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerDecoration
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerDecorator
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.internal.TestContainerDecorator
import org.orbitmvi.orbit.orbitContainer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class OrbitEventObserverTest {

    private val observer = RecordingObserver()

    @Test
    fun container_creation_reports_id_name_and_initial_state() = runTest {
        backgroundScope.orbitContainer<Int, Nothing>(
            initialState = 42,
            buildSettings = {
                containerName = "MyContainer"
                eventObserver = observer
            }
        )

        val created = observer.eventsOf<RecordedEvent.Created>().single()
        assertEquals("MyContainer", created.containerName)
        assertEquals(42, created.initialState)
    }

    @Test
    fun container_ids_are_unique_across_containers() = runTest {
        backgroundScope.orbitContainer<Int, Nothing>(1, buildSettings = { eventObserver = observer })
        backgroundScope.orbitContainer<Int, Nothing>(2, buildSettings = { eventObserver = observer })

        val created = observer.eventsOf<RecordedEvent.Created>()
        assertEquals(2, created.size)
        assertNotEquals(created[0].containerId, created[1].containerId)
    }

    @Test
    fun container_closed_reported_when_scope_completes() = runTest {
        val scope = CoroutineScope(Job())
        val container = scope.orbitContainer<Int, Nothing>(0, buildSettings = { eventObserver = observer })
        container.orbit { reduce { it + 1 } }
        container.joinIntents()

        assertTrue(observer.eventsOf<RecordedEvent.Closed>().isEmpty())

        scope.cancel()
        scope.coroutineContext[Job]?.join()

        val created = observer.eventsOf<RecordedEvent.Created>().single()
        val closed = observer.eventsOf<RecordedEvent.Closed>().single()
        assertEquals(created.containerId, closed.containerId)
    }

    @Test
    fun intent_dispatch_and_completion_reported() = runTest {
        val host = TestHost(backgroundScope, observer)
        host.increment()
        host.container.joinIntents()

        val dispatched = observer.eventsOf<RecordedEvent.Dispatched>().single()
        val completed = observer.eventsOf<RecordedEvent.Completed>().single()
        assertEquals(dispatched.intentId, completed.intentId)
        assertEquals(dispatched.containerId, completed.containerId)
        assertIs<IntentResult.Completed>(completed.result)
    }

    @Test
    fun reductions_attributed_to_their_intent() = runTest {
        val host = TestHost(backgroundScope, observer)
        host.increment()
        host.container.joinIntents()

        val dispatched = observer.eventsOf<RecordedEvent.Dispatched>().single()
        val reduction = observer.eventsOf<RecordedEvent.Reduction>().single()
        assertEquals(dispatched.intentId, reduction.intentId)
        assertEquals(0, reduction.oldState)
        assertEquals(1, reduction.newState)
    }

    @Test
    fun side_effects_attributed_to_their_intent() = runTest {
        val host = TestHost(backgroundScope, observer)
        host.notify("ping")
        host.container.joinIntents()

        val dispatched = observer.eventsOf<RecordedEvent.Dispatched>().single()
        val sideEffect = observer.eventsOf<RecordedEvent.SideEffect>().single()
        assertEquals(dispatched.intentId, sideEffect.intentId)
        assertEquals("ping", sideEffect.sideEffect)
    }

    @Test
    fun sub_intent_work_attributed_to_the_launching_intent() = runTest {
        val host = TestHost(backgroundScope, observer)
        host.incrementViaSubIntent()
        host.container.joinIntents()

        val dispatched = observer.eventsOf<RecordedEvent.Dispatched>().single()
        val reduction = observer.eventsOf<RecordedEvent.Reduction>().single()
        assertEquals(dispatched.intentId, reduction.intentId)
    }

    @Test
    fun run_on_work_attributed_to_the_launching_intent() = runTest {
        val sealedContainer = backgroundScope.orbitContainer<SealedState, Nothing>(
            initialState = SealedState.Counting(0),
            buildSettings = { eventObserver = observer }
        )
        val host = object : OrbitContainerHost<SealedState, SealedState, Nothing> {
            override val container: OrbitContainer<SealedState, SealedState, Nothing> = sealedContainer
        }

        host.intent {
            runOn<SealedState.Counting> {
                reduce { state.copy(count = state.count + 1) }
            }
        }
        host.container.joinIntents()

        val dispatched = observer.eventsOf<RecordedEvent.Dispatched>().single()
        val reduction = observer.eventsOf<RecordedEvent.Reduction>().single()
        assertEquals(dispatched.intentId, reduction.intentId)
        assertEquals(SealedState.Counting(1), reduction.newState)
    }

    @Test
    fun on_create_intent_reported_with_name_and_attribution() = runTest {
        val container = backgroundScope.orbitContainer<Int, Nothing>(
            initialState = 0,
            buildSettings = { eventObserver = observer }
        ) {
            reduce { state + 1 }
        }

        container.stateFlow.test {
            // The lazy onCreate intent races the initial emission; either 0 then 1, or just 1
            var item = awaitItem()
            if (item != 1) item = awaitItem()
            assertEquals(1, item)
        }
        container.joinIntents()

        val dispatched = observer.eventsOf<RecordedEvent.Dispatched>().single()
        assertEquals("onCreate", dispatched.name)
        val reduction = observer.eventsOf<RecordedEvent.Reduction>().single()
        assertEquals(dispatched.intentId, reduction.intentId)
    }

    @Test
    fun failed_intent_reported_even_when_exception_handler_consumes() = runTest {
        val exception = IllegalStateException("boom")
        val container = backgroundScope.orbitContainer<Int, Nothing>(
            initialState = 0,
            buildSettings = {
                eventObserver = observer
                exceptionHandler = CoroutineExceptionHandler { _, _ -> }
            }
        )

        container.orbit { throw exception }
        container.joinIntents()

        val completed = observer.eventsOf<RecordedEvent.Completed>().single()
        val result = assertIs<IntentResult.Failed>(completed.result)
        assertSame(exception, result.exception)
    }

    @Test
    fun cancelled_intent_reported_as_cancelled() = runTest {
        val container = backgroundScope.orbitContainer<Int, Nothing>(
            initialState = 0,
            buildSettings = { eventObserver = observer }
        )

        container.stateFlow.test {
            skipItems(1)
            val job = container.orbit { delay(Long.MAX_VALUE) }
            // A second intent reducing proves the first was dispatched before we cancel it
            container.orbit { reduce { it + 1 } }
            assertEquals(1, awaitItem())
            job.cancel()
        }
        container.joinIntents()

        val results = observer.eventsOf<RecordedEvent.Completed>().map { it.result }
        assertTrue(results.any { it is IntentResult.Cancelled }, "expected a Cancelled result in $results")
    }

    @Test
    fun no_op_reductions_are_reported() = runTest {
        val container = backgroundScope.orbitContainer<Int, Nothing>(
            initialState = 0,
            buildSettings = { eventObserver = observer }
        )

        container.orbit { reduce { it } }
        container.joinIntents()

        val reduction = observer.eventsOf<RecordedEvent.Reduction>().single()
        assertEquals(reduction.oldState, reduction.newState)
    }

    @Test
    fun reductions_outside_tracked_intents_have_null_intent_id() = runTest {
        val container = backgroundScope.orbitContainer<Int, Nothing>(
            initialState = 0,
            buildSettings = { eventObserver = observer }
        )

        container.inlineOrbit { reduce { it + 1 } }

        val reduction = observer.eventsOf<RecordedEvent.Reduction>().single()
        assertNull(reduction.intentId)
    }

    @Test
    fun attribution_does_not_leak_across_containers() = runTest {
        val other = backgroundScope.orbitContainer<Int, Nothing>(
            initialState = 0,
            buildSettings = { eventObserver = observer }
        )
        val host = TestHost(backgroundScope, observer)

        host.intent {
            other.inlineOrbit { reduce { it + 1 } }
        }
        host.container.joinIntents()

        val otherCreated = observer.eventsOf<RecordedEvent.Created>().first()
        val reduction = observer.eventsOf<RecordedEvent.Reduction>().single { it.containerId == otherCreated.containerId }
        assertNull(reduction.intentId)
    }

    @Test
    fun observer_installation_does_not_change_observable_behaviour() = runTest {
        val observed = TestHost(backgroundScope, observer)
        val unobserved = TestHost(backgroundScope, eventObserver = null)

        suspend fun script(host: TestHost): Pair<List<Int>, String> {
            var states = emptyList<Int>()
            var sideEffect = ""
            host.container.stateFlow.test {
                host.increment()
                host.increment()
                host.notify("done")
                states = listOf(awaitItem(), awaitItem(), awaitItem())
            }
            host.container.sideEffectFlow.test {
                sideEffect = awaitItem()
            }
            return states to sideEffect
        }

        assertEquals(script(unobserved), script(observed))
    }

    @Test
    fun container_decoration_applied_outermost() = runTest {
        val decoration = MarkerDecoration()
        val container = backgroundScope.orbitContainer<Int, Nothing>(
            initialState = 0,
            buildSettings = { containerDecoration = decoration }
        )

        assertSame(decoration.decorated, container)
        val decorator = assertIs<OrbitContainerDecorator<Int, Int, Nothing>>(container)
        assertIs<TestContainerDecorator<Int, Int, Nothing>>(decorator.actual)

        // The decorated container still works
        container.stateFlow.test {
            assertEquals(0, awaitItem())
            container.orbit { reduce { it + 1 } }
            assertEquals(1, awaitItem())
        }
    }

    private class TestHost(
        scope: CoroutineScope,
        eventObserver: OrbitEventObserver?
    ) : OrbitContainerHost<Int, Int, String> {
        override val container: OrbitContainer<Int, Int, String> = scope.orbitContainer(
            initialState = 0,
            buildSettings = { this.eventObserver = eventObserver }
        )

        fun increment() = intent { reduce { state + 1 } }

        fun notify(message: String) = intent { postSideEffect(message) }

        fun incrementViaSubIntent() = intent { subIntent { reduce { state + 1 } } }
    }

    private sealed interface SealedState {
        data class Counting(val count: Int) : SealedState
    }

    private class MarkerDecoration : ContainerDecoration {
        var decorated: OrbitContainer<*, *, *>? = null

        override fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> decorate(
            container: OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>
        ): OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
            val decorator = object : OrbitContainerDecorator<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> {
                override val actual: OrbitContainer<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT> = container
            }
            decorated = decorator
            return decorator
        }
    }
}
