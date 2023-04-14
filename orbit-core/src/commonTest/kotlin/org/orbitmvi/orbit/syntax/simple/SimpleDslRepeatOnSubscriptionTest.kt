/*
 * Copyright 2021-2023 Mikołaj Leszczyński & Appmattus Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription
import org.orbitmvi.orbit.internal.repeatonsubscription.TestSubscribedCounter
import org.orbitmvi.orbit.syntax.ContainerContext

internal class SimpleDslRepeatOnSubscriptionTest {

    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    private val count = atomic(0)

    private val testSubscribedCounter = TestSubscribedCounter()

    private val simpleSyntax = SimpleSyntax(
        containerContext = ContainerContext<Unit, Unit>(
            settings = RealSettings(),
            postSideEffect = { _, _ -> },
            getState = {},
            reduce = { _, _ -> },
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
