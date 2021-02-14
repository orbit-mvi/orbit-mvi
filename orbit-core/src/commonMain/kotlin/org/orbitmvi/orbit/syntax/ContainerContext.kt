package org.orbitmvi.orbit.syntax

import org.orbitmvi.orbit.Container

public class ContainerContext<S : Any, SE : Any>(
    public val settings: Container.Settings,
    public val postSideEffect: suspend (SE) -> Unit,
    private val getState: () -> S,
    public val reduce: suspend ((S) -> S) -> Unit
) {
    public val state: S
        get() = getState()
}