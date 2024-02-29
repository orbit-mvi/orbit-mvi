package org.orbitmvi.orbit.syntax.simple

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription
import org.orbitmvi.orbit.internal.repeatonsubscription.TestSubscribedCounter
import org.orbitmvi.orbit.syntax.ContainerContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SimpleDslRepeatOnSubscriptionTest {

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    private val count = atomic(0)

    private val testSubscribedCounter = TestSubscribedCounter()

    private val simpleSyntax = SimpleSyntax(
        containerContext = ContainerContext<Unit, Unit>(
            settings = RealSettings(),
            postSideEffect = {},
            getState = {},
            reduce = {},
            subscribedCounter = testSubscribedCounter
        )
    )

    @AfterTest
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun repeatOnSubscription_block_called_for_each_subscribed_event() {
        testScope.launch {
            simpleSyntax.repeatOnSubscription {
                count.incrementAndGet()
            }
        }

        testSubscribedCounter.flow.value = Subscription.Subscribed
        testSubscribedCounter.flow.value = Subscription.Unsubscribed
        testSubscribedCounter.flow.value = Subscription.Subscribed

        assertEquals(2, count.value)
    }

    @Test
    fun repeatOnSubscription_cancels_running_job_for_unsubscribed_event() {
        testScope.launch {
            simpleSyntax.repeatOnSubscription {
                try {
                    delay(5000L)
                } catch (expected: CancellationException) {
                    count.incrementAndGet()
                    throw expected
                }
            }
        }

        testSubscribedCounter.flow.value = Subscription.Subscribed
        testSubscribedCounter.flow.value = Subscription.Unsubscribed

        assertEquals(1, count.value)
    }
}
