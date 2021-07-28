package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Subscribed
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Unsubscribed
import org.orbitmvi.orbit.test
import org.orbitmvi.orbit.test.runBlocking

class DelayingSubscribedCounterTest {

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun `initial value is unsubscribed`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 0)
            val testObserver = counter.subscribed.test()

            assertEquals(listOf(Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun `incrementing subscribes`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 0)
            val testObserver = counter.subscribed.test()

            counter.increment()

            testObserver.awaitCount(2)
            assertEquals(listOf(Unsubscribed, Subscribed), testObserver.values)
        }
    }

    @Test
    fun `increment decrement unsubscribes`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 0)
            val testObserver = counter.subscribed.test()

            counter.increment()
            counter.decrement()

            testObserver.awaitCount(3)
            assertEquals(listOf(Unsubscribed, Subscribed, Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun `values received are distinct`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 0)
            val testObserver = counter.subscribed.test()

            counter.increment()
            counter.increment()
            counter.decrement()
            counter.decrement()

            testObserver.awaitCount(3)
            assertEquals(listOf(Unsubscribed, Subscribed, Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun `unsubscribed received on launch immediately`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 500)
            val testObserver = counter.subscribed.mapTimed().test()

            testObserver.awaitCount(1)
            assertEquals(Unsubscribed, testObserver.values.first().value)
            assertTrue(testObserver.values.first().time < 500)
        }
    }

    @Test
    fun `unsubscribed received after delay`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 500)
            val testObserver = counter.subscribed.mapTimed().test()

            counter.increment()
            counter.decrement()

            testObserver.awaitCount(3)
            assertEquals(listOf(Unsubscribed, Subscribed, Unsubscribed), testObserver.values.map { it.value })
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
