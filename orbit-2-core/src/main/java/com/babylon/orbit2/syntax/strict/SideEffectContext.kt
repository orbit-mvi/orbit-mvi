package com.babylon.orbit2.syntax.strict

import com.babylon.orbit2.Container
import com.babylon.orbit2.syntax.Operator
import com.babylon.orbit2.syntax.Orbit2Dsl

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
