package org.orbitmvi.orbit.syntax.simple

import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.idling.withIdling

public actual fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.blockingIntent(
    registerIdling: Boolean,
    transformer: suspend SimpleSyntax<STATE, SIDE_EFFECT>.() -> Unit
) {
    container.orbit {
        withIdling(registerIdling) {
            SimpleSyntax(this).transformer()
        }
    }
}
