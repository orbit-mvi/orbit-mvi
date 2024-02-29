package org.orbitmvi.orbit.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Runs [withTimeout] in realtime.
 * This is used in Tests, because time passing is instant there, thus making timeouts impossible.
 */
@OptIn(ExperimentalCoroutinesApi::class)
public suspend fun <T> withTimeoutRealtime(timeMillis: Long, block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Default.limitedParallelism(1)) {
        withTimeout(timeMillis, block)
    }
