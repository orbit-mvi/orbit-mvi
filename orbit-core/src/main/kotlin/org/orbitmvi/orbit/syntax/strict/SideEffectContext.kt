package org.orbitmvi.orbit.syntax.strict

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.syntax.Operator
import org.orbitmvi.orbit.syntax.Orbit2Dsl

/**
 * Represents the current context in which a side effect [Operator] is executing.
 */
@Orbit2Dsl
public interface SideEffectContext<S : Any, SE : Any, E> : Context<S, E> {
    /**
     * Posts a side effect to [Container.sideEffectFlow].
     */
    public suspend fun post(event: SE)
}
