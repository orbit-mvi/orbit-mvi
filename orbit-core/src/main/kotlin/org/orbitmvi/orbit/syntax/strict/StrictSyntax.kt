package org.orbitmvi.orbit.syntax.strict

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Orbit2Dsl
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
