package org.orbitmvi.orbit.idling

import org.orbitmvi.orbit.syntax.Operator
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

public suspend fun <O : Operator<*, *>, T> OrbitDslPlugin.ContainerContext<*, *>.withIdling(
    operator: O,
    block: suspend O.() -> T
): T {
    if (operator.registerIdling) settings.idlingRegistry.increment()
    return block(operator).also {
        if (operator.registerIdling) settings.idlingRegistry.decrement()
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
public suspend fun <O : Operator<*, *>, T> OrbitDslPlugin.ContainerContext<*, *>.withIdlingFlow(
    operator: O,
    block: suspend O.() -> Flow<T>
): Flow<T> {
    return block(operator)
        .onStart { if (operator.registerIdling) settings.idlingRegistry.increment() }
        .onCompletion { if (operator.registerIdling) settings.idlingRegistry.decrement() }
}
