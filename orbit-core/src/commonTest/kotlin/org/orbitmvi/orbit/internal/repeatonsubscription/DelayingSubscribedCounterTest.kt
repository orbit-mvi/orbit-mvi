package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Subscribed
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Unsubscribed
import org.orbitmvi.orbit.test.runBlocking
import org.orbitmvi.orbit.testFlowObserver
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DelayingSubscribedCounterTest {

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun initial_value_is_unsubscribed() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 0)
            val testObserver = counter.subscribed.testFlowObserver()

            assertEquals(listOf(Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun incrementing_subscribes() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 0)
            val testObserver = counter.subscribed.testFlowObserver()

            counter.increment()

            testObserver.awaitCount(2)
            assertEquals(listOf(Unsubscribed, Subscribed), testObserver.values)
        }
    }

    @Test
    fun increment_decrement_unsubscribes() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 0)
            val testObserver = counter.subscribed.testFlowObserver()

            counter.increment()
            counter.decrement()

            testObserver.awaitCount(3)
            assertEquals(listOf(Unsubscribed, Subscribed, Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun values_received_are_distinct() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 0)
            val testObserver = counter.subscribed.testFlowObserver()

            counter.increment()
            counter.increment()
            counter.decrement()
            counter.decrement()

            testObserver.awaitCount(3)
            assertEquals(listOf(Unsubscribed, Subscribed, Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun negative_decrements_are_ignored() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 0)
            val testObserver = counter.subscribed.testFlowObserver()

            counter.decrement()
            counter.decrement()
            counter.decrement()
            counter.increment()
            counter.decrement()

            testObserver.awaitCount(3)
            assertEquals(listOf(Unsubscribed, Subscribed, Unsubscribed), testObserver.values)
        }
    }

    @Test
    fun unsubscribed_received_on_launch_immediately() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 500)
            val testObserver = counter.subscribed.mapTimed().testFlowObserver()

            testObserver.awaitCount(1)
            assertEquals(Unsubscribed, testObserver.values.first().value)
            assertTrue(testObserver.values.first().time < 450)
        }
    }

    @Test
    fun unsubscribed_received_immediately_on_second_observation() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 500)

            // Wait to receive unsubscribed at launch
            counter.subscribed.mapTimed().testFlowObserver().awaitCount(1)

            val testObserver2 = counter.subscribed.mapTimed().testFlowObserver()
            testObserver2.awaitCount(1)
            assertEquals(Unsubscribed, testObserver2.values.first().value)
            assertTrue(testObserver2.values.first().time < 450)
        }
    }

    @Test
    fun unsubscribed_received_after_delay() {
        runBlocking {
            val counter = DelayingSubscribedCounter(testScope, 500)
            val testObserver = counter.subscribed.mapTimed().testFlowObserver()

            counter.increment()
            counter.decrement()

            testObserver.awaitCount(3)
            assertEquals(listOf(Unsubscribed, Subscribed, Unsubscribed), testObserver.values.map { it.value })
            assertTrue(testObserver.values.last().time > 450)
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
