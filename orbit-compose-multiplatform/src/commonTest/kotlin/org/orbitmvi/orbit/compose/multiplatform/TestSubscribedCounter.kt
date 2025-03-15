package org.orbitmvi.orbit.compose.multiplatform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription

public class TestSubscribedCounter : SubscribedCounter {
    public var counter: Int = 0

    public val flow: MutableStateFlow<Subscription> = MutableStateFlow(Subscription.Unsubscribed)
    override val subscribed: Flow<Subscription> = flow.asStateFlow()

    override suspend fun increment() {
        counter++
    }

    override suspend fun decrement() {
        counter--
    }
}
