package com.babylon.orbit2.idling

import com.babylon.orbit2.syntax.Operator
import com.babylon.orbit2.syntax.strict.OrbitDslPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

suspend fun <O : Operator<*, *>, T> OrbitDslPlugin.ContainerContext<*, *>.withIdling(operator: O, block: suspend O.() -> T): T {
    if (operator.registerIdling) settings.idlingRegistry.increment()
    return block(operator).also {
        if (operator.registerIdling) settings.idlingRegistry.decrement()
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
suspend fun <O : Operator<*, *>, T> OrbitDslPlugin.ContainerContext<*, *>.withIdlingFlow(operator: O, block: suspend O.() -> Flow<T>): Flow<T> {
    return block(operator)
        .onStart { if (operator.registerIdling) settings.idlingRegistry.increment() }
        .onCompletion { if (operator.registerIdling) settings.idlingRegistry.decrement() }
}
