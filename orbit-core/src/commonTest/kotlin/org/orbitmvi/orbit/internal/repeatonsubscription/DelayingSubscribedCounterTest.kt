package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Subscribed
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Unsubscribed
import org.orbitmvi.orbit.test
import org.orbitmvi.orbit.test.runBlocking

class DelayingSubscribedCounterTest {

    @Test
    fun `initial value is unsubscribed`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(0)
            val testObserver = counter.subscribed.test()

            assertEquals(listOf(Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun `incrementing subscribes`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(0)
            val testObserver = counter.subscribed.test()

            counter.increment()

            assertEquals(listOf(Unsubscribed, Subscribed), testObserver.values)
        }
    }

    @Test
    fun `increment decrement unsubscribes`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(0)
            val testObserver = counter.subscribed.test()

            counter.increment()
            counter.decrement()

            assertEquals(listOf(Unsubscribed, Subscribed, Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun `values received are distinct`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(0)
            val testObserver = counter.subscribed.test()

            counter.increment()
            counter.increment()
            counter.decrement()
            counter.decrement()

            assertEquals(listOf(Unsubscribed, Subscribed, Unsubscribed), testObserver.values)
        }
    }

    // Undesirable behaviour - ideally we would receive the first unsubscribed value immediately
    @Test
    fun `unsubscribed received on launch after delay`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(500)
            val testObserver = counter.subscribed.mapTimed().test()

            testObserver.awaitCount(1)
            assertEquals(Unsubscribed, testObserver.values.first().value)
            assertTrue(testObserver.values.first().time in 450..550)
        }
    }

    @Test
    fun `unsubscribed received after delay`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(500)
            val testObserver = counter.subscribed.mapTimed().test()

            counter.increment()
            counter.decrement()

            testObserver.awaitCount(2)
            assertEquals(listOf(Subscribed, Unsubscribed), testObserver.values.map { it.value })
            assertTrue(testObserver.values.last().time in 450..550)
        }
    }

    private fun Flow<Subscription>.mapTimed(): Flow<Timed<Subscription>> {
        var last = getSystemTimeInMillis()
        return map {
            val current = getSystemTimeInMillis()
            val diff = current - last
            last = current
            Timed(diff, it)
        }
    }

    private data class Timed<T>(val time: Long, val value: T)
}
