package com.babylon.orbit2

/**
 * Represents the current context in which a side effect [Operator] is executing.
 */
@Orbit2Dsl
interface SideEffectContext<S : Any, SE : Any, E> : Context<S, E> {
    /**
     * Posts a side effect to [Container.sideEffectStream].
     */
    fun post(event: SE)
}
