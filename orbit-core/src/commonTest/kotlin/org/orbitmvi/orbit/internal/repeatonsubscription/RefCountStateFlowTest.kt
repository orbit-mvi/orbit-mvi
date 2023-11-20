package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import org.orbitmvi.orbit.test.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class RefCountStateFlowTest {

    @Test
    fun increments_on_collection_and_decrements_once_completed() {
        val subscribedCounter = TestSubscribedCounter()

        runBlocking {
            assertEquals(0, subscribedCounter.counter)

            MutableStateFlow(Unit).refCount(subscribedCounter).take(1).collect {
                assertEquals(1, subscribedCounter.counter)
            }

            assertEquals(0, subscribedCounter.counter)
        }
    }

    @Test
    fun decrements_even_after_exception() {
        val subscribedCounter = TestSubscribedCounter()

        runBlocking {
            try {
                MutableStateFlow(Unit).refCount(subscribedCounter).take(1).collect {
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
