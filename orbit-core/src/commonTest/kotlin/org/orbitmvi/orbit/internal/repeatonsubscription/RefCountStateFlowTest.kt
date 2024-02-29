package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RefCountStateFlowTest {

    @Test
    fun increments_on_collection_and_decrements_once_completed() = runTest {
        val subscribedCounter = TestSubscribedCounter()

        assertEquals(0, subscribedCounter.counter)

        MutableStateFlow(Unit).refCount(subscribedCounter).take(1).collect {
            assertEquals(1, subscribedCounter.counter)
        }

        assertEquals(0, subscribedCounter.counter)
    }

    @Test
    fun decrements_even_after_exception() = runTest {
        val subscribedCounter = TestSubscribedCounter()

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
