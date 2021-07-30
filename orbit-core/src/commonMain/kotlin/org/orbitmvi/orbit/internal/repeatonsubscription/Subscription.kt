package org.orbitmvi.orbit.internal.repeatonsubscription

public enum class Subscription {
    Unsubscribed,
    Subscribed;

    public val isSubscribed: Boolean
        get() = this == Subscribed
}
