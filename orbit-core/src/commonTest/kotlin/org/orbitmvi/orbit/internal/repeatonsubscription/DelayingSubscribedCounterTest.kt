package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    fun `unsubscribed not received immediately on launch when delayed`() {
        runBlocking(Dispatchers.Default) {
            val counter = DelayingSubscribedCounter(100)
            val testObserver = counter.subscribed.test()

            assertEquals(emptyList(), testObserver.values)
        }
    }

    // Undesirable behaviour - ideally we would receive the first unsubscribed value immediately
    @Test
    fun `unsubscribed received on launch after delay`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(100)
            val testObserver = counter.subscribed.test()

            delay(200)

            assertEquals(listOf(Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun `unsubscribed not received immediately when delayed`() {
        runBlocking(Dispatchers.Default) {
            val counter = DelayingSubscribedCounter(100)
            val testObserver = counter.subscribed.test()

            counter.increment()
            counter.decrement()

            assertEquals(listOf(Subscribed), testObserver.values)
        }
    }

    @Test
    fun `unsubscribed received after delay`() {
        runBlocking {
            val counter = DelayingSubscribedCounter(100)
            val testObserver = counter.subscribed.test()

            counter.increment()
            counter.decrement()
            delay(200)
            assertEquals(listOf(Subscribed, Unsubscribed), testObserver.values)
        }
    }
}
