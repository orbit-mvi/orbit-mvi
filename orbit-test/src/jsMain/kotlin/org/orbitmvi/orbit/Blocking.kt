package org.orbitmvi.orbit

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal actual fun runBlocking(block: suspend () -> Unit) = runBlocking(Dispatchers.Default, block)

internal actual fun runBlocking(coroutineContext: CoroutineContext, block: suspend () -> Unit) {
    CoroutineScope(coroutineContext).launch { block() }
}
