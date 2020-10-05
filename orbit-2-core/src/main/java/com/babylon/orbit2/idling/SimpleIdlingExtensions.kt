package com.babylon.orbit2.idling

import com.babylon.orbit2.syntax.strict.OrbitDslPlugin

suspend fun <STATE : Any, SIDE_EFFECT : Any> OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.withIdling(
    registerIdling: Boolean,
    block: suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit
) {
    if (registerIdling) settings.idlingRegistry.increment()
    return block().also {
        if (registerIdling) settings.idlingRegistry.decrement()
    }
}
