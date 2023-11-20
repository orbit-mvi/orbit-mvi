package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import org.orbitmvi.orbit.test.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class RefCountFlowTest {

    @Test
    fun increments_on_collection_and_decrements_once_completed() {
        runBlocking {
            val subscribedCounter = TestSubscribedCounter()

            assertEquals(0, subscribedCounter.counter)

            flowOf(Unit).refCount(subscribedCounter).take(1).collect {
                assertEquals(1, subscribedCounter.counter)
            }

            assertEquals(0, subscribedCounter.counter)
        }
    }

    @Test
    fun decrements_even_after_exception() {
        runBlocking {
            val subscribedCounter = TestSubscribedCounter()

            try {
                flowOf(Unit).refCount(subscribedCounter).take(1).collect {
                    assertEquals(1, subscribedCounter.counter)
                    error("forced exception")
                }
            } catch (ignore: IllegalStateException) {
                // ignored as we just care that counter is decremented
            }

            assertEquals(0, subscribedCounter.counter)
        }
    }
}
