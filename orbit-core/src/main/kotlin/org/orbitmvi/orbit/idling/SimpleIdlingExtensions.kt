package org.orbitmvi.orbit.idling

import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin

public suspend fun <STATE : Any, SIDE_EFFECT : Any> OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.withIdling(
    registerIdling: Boolean,
    block: suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit
) {
    if (registerIdling) settings.idlingRegistry.increment()
    return block().also {
        if (registerIdling) settings.idlingRegistry.decrement()
    }
}
