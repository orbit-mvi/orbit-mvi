package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import org.orbitmvi.orbit.test.runBlocking

class RefCountFlowTest {

    @Test
    fun `increments on collection and decrements once completed`() {
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
    fun `decrements even after exception`() {
        runBlocking {
            val subscribedCounter = TestSubscribedCounter()

            try {
                flowOf(Unit).refCount(subscribedCounter).take(1).collect {
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
