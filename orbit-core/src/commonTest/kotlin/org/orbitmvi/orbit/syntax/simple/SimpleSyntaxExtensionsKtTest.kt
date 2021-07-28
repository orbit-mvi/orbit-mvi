package org.orbitmvi.orbit.syntax.simple

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription
import org.orbitmvi.orbit.internal.repeatonsubscription.TestSubscribedCounter
import org.orbitmvi.orbit.syntax.ContainerContext

internal class SimpleSyntaxExtensionsKtTest {

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    private val count = atomic(0)

    private val testSubscribedCounter = TestSubscribedCounter()

    private val simpleSyntax = SimpleSyntax(
        containerContext = ContainerContext<Unit, Unit>(
            settings = Container.Settings(),
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
    fun `repeatOnSubscription block called for each subscribed event`() {
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
    fun `repeatOnSubscription cancels running job for unsubscribed event`() {
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
