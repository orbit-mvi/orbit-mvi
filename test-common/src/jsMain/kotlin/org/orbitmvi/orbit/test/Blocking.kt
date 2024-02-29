package org.orbitmvi.orbit.test

import kotlin.coroutines.CoroutineContext

public actual fun <T> runBlocking(block: suspend () -> T): T = TODO()
public actual fun <T> runBlocking(coroutineContext: CoroutineContext, block: suspend () -> T): T = TODO()
