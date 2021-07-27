package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.flow.Flow

public interface SubscribedCounter {

    public val subscribed: Flow<Subscription>

    public suspend fun increment()

    public suspend fun decrement()
}
