package org.orbitmvi.orbit.test

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun <T> withAppropriateTimeout(
    timeout: Duration,
    block: suspend CoroutineScope.() -> T,
): T {
    return if (coroutineContext[TestCoroutineScheduler] != null) {
        // withTimeout uses virtual time, which will hang.
        withWallClockTimeout(timeout, block)
    } else {
        withTimeout(timeout, block)
    }
}

private suspend fun <T> withWallClockTimeout(
    timeout: Duration,
    block: suspend CoroutineScope.() -> T,
): T = coroutineScope {
    val blockDeferred = async(start = CoroutineStart.UNDISPATCHED, block = block)

    // Run the timeout on a scope separate from the caller. This ensures that the use of the
    // Default dispatcher does not affect the use of a TestScheduler and its fake time.
    @OptIn(DelicateCoroutinesApi::class)
    val timeoutJob = GlobalScope.launch(Dispatchers.Default) { delay(timeout) }

    select {
        blockDeferred.onAwait { result ->
            timeoutJob.cancel()
            result
        }
        timeoutJob.onJoin {
            blockDeferred.cancel()
            throw OrbitTimeoutCancellationException("Timed out waiting for remaining intents to complete for $timeout")
        }
    }
}

internal class OrbitTimeoutCancellationException internal constructor(
    message: String,
) : CancellationException(message)
