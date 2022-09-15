package org.orbitmvi.orbit.syntax

import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter

@OrbitInternal
public class ContainerContext<S : Any, SE : Any>(
    public val settings: RealSettings,
    public val postSideEffect: suspend (SE) -> Unit,
    private val getState: () -> S,
    public val reduce: suspend ((S) -> S) -> Unit,
    public val subscribedCounter: SubscribedCounter
) {
    public val state: S
        get() = getState()
}
