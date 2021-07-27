package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class CountingSubscribedCounter : SubscribedCounter {
    var counter = 0

    override val subscribed: Flow<Subscription> = flowOf(Subscription.Unsubscribed)

    override suspend fun increment() {
        counter++
    }

    override suspend fun decrement() {
        counter--
    }
}
