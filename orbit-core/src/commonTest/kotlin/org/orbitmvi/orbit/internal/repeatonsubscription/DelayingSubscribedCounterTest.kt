package org.orbitmvi.orbit.internal.repeatonsubscription

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Subscribed
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription.Unsubscribed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DelayingSubscribedCounterTest {

    @Test
    fun `initial value is unsubscribed`() = runTest {
        val counter = DelayingSubscribedCounter(backgroundScope, 0)
        counter.subscribed.test {
            assertEquals(Unsubscribed, awaitItem())
        }
    }

    @Test
    fun `incrementing subscribes`() = runTest {
        val counter = DelayingSubscribedCounter(backgroundScope, 0)

        counter.subscribed.test {
            counter.increment()
            assertEquals(Unsubscribed, awaitItem())
            assertEquals(Subscribed, awaitItem())
        }
    }

    @Test
    fun `increment decrement unsubscribes`() = runTest {
        val counter = DelayingSubscribedCounter(backgroundScope, 0)

        counter.subscribed.test {
            counter.increment()
            counter.decrement()
            assertEquals(Unsubscribed, awaitItem())
            assertEquals(Subscribed, awaitItem())
            assertEquals(Unsubscribed, awaitItem())
        }
    }

    @Test
    fun `values received are distinct`() = runTest {
        val counter = DelayingSubscribedCounter(backgroundScope, 0)

        counter.subscribed.test {
            counter.increment()
            counter.increment()
            counter.decrement()
            counter.decrement()

            assertEquals(Unsubscribed, awaitItem())
            assertEquals(Subscribed, awaitItem())
            assertEquals(Unsubscribed, awaitItem())
        }
    }

    @Test
    fun `negative decrements are ignored`() = runTest {
        val counter = DelayingSubscribedCounter(backgroundScope, 0)

        counter.subscribed.test {
            counter.decrement()
            counter.decrement()
            counter.decrement()
            counter.increment()
            counter.decrement()

            assertEquals(Unsubscribed, awaitItem())
            assertEquals(Subscribed, awaitItem())
            assertEquals(Unsubscribed, awaitItem())
        }
    }

    @Test
    fun `unsubscribed received on launch immediately`() = runTest {
        val innerScope = TestScope()
        val counter = DelayingSubscribedCounter(innerScope, 500)
        counter.subscribed.mapTimed(innerScope.testScheduler).test {
            innerScope.advanceUntilIdle()
            val item = awaitItem()

            assertEquals(Unsubscribed, item.value)
            assertTrue(item.time < 450)
        }
    }

    @Test
    fun `unsubscribed received immediately on second observation`() = runTest {
        val innerScope = TestScope()
        val counter = DelayingSubscribedCounter(backgroundScope, 500)

        counter.subscribed.mapTimed(innerScope.testScheduler).test {
            innerScope.advanceUntilIdle()
            // Wait to receive unsubscribed at launch
            skipItems(1)
        }

        counter.subscribed.mapTimed(innerScope.testScheduler).test {
            innerScope.advanceUntilIdle()
            val item = awaitItem()

            assertEquals(Unsubscribed, item.value)
            assertTrue(item.time < 450)
        }
    }

    @Test
    fun `unsubscribed received after delay`() = runTest {
        val innerScope = TestScope()
        val counter = DelayingSubscribedCounter(innerScope, 500)

        counter.subscribed.mapTimed(innerScope.testScheduler).test {
            assertEquals(Unsubscribed, awaitItem().value)
            counter.increment()
            innerScope.advanceUntilIdle()
            assertEquals(Subscribed, awaitItem().value)
            counter.decrement()
            innerScope.advanceUntilIdle()

            val item = awaitItem()
            assertEquals(Unsubscribed, item.value)
            assertTrue(item.time > 450)
        }
    }

    private fun Flow<Subscription>.mapTimed(testCoroutineScheduler: TestCoroutineScheduler): Flow<Timed<Subscription>> {
        var last = testCoroutineScheduler.currentTime
        return map {
            val current = testCoroutineScheduler.currentTime
            val diff = current - last
            last = current
            Timed(diff, it)
        }
    }

    private data class Timed<T>(val time: Long, val value: T)
}
