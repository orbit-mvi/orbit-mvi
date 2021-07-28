package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import org.orbitmvi.orbit.test.runBlocking

class RefCountStateFlowTest {

    @Test
    fun `increments on collection and decrements once completed`() {
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
    fun `decrements even after exception`() {
        val subscribedCounter = TestSubscribedCounter()

        runBlocking {
            try {
                MutableStateFlow(Unit).refCount(subscribedCounter).take(1).collect {
                    assertEquals(1, subscribedCounter.counter)
                    throw IllegalStateException("forced exception")
                }
            } catch (ignore: IllegalStateException) {
                // ignored as we just care that counter is decremented
            }

            assertEquals(0, subscribedCounter.counter)
        }
    }
}
