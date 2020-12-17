package com.babylon.orbit2.syntax.strict

import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.syntax.Orbit2Dsl
import kotlinx.coroutines.flow.collect

/**
 * Build and execute an orbit flow on [Container] using the [Builder] and
 * associated DSL functions.
 *
 * @param init lambda returning the operator chain that represents the flow
 */
@Orbit2Dsl
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.orbit(
    init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>
): Unit = container.orbit {
    Builder<STATE, SIDE_EFFECT, Unit>()
        .init()
        .build(this)
        .collect()
}
