package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class TestSubscribedCounter : SubscribedCounter {
    var counter = 0

    val flow = MutableStateFlow(Subscription.Unsubscribed)
    override val subscribed: Flow<Subscription> = flow.asStateFlow()

    override suspend fun increment() {
        counter++
    }

    override suspend fun decrement() {
        counter--
    }
}
