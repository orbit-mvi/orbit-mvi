package com.babylon.orbit2.uitest.gradle

import kotlin.math.pow
import kotlin.reflect.KClass

fun <T> retryWithExponentialBackoff(allowedExceptions: Set<KClass<out Exception>> = emptySet(), cb: () -> T): T = ExponentialBackoff(
    maxAttempts = MAX_ATTEMPTS,
    maxDelayInMs = MAX_DELAY_IN_MS,
    allowedExceptions = allowedExceptions.toSet()
).retry(cb)

private class ExponentialBackoff(
    private val maxAttempts: Int,
    private val maxDelayInMs: Long,
    private val allowedExceptions: Set<KClass<out Exception>>
) {

    fun <T> retry(cb: () -> T): T {
        var lastException: Exception? = null

        repeat(maxAttempts + 1) {
            val delay = calculateExponentialDelay(it, maxDelayInMs)
            Thread.sleep(delay)

            try {
                return cb()
            } catch (expected: Exception) {
                lastException = expected
                if (allowedExceptions.contains(expected::class)) {
                    throw expected
                }
            }
        }
        throw IllegalArgumentException("Max attempts has been reached with number $maxAttempts", lastException)
    }

    @Suppress("MagicNumber")
    private fun calculateExponentialDelay(
        failedAttempts: Int,
        maxDelayInMs: Long
    ): Long {
        // Taken from wikipedia
        // https://en.wikipedia.org/wiki/Exponential_backoff
        val delayInMs = (2.0.pow(failedAttempts) - 1.0) / 2.0 * 1000
        // Just use maximum back-off value in case the delay is too big
        return if (maxDelayInMs < delayInMs) maxDelayInMs else delayInMs.toLong()
    }
}

private const val MAX_ATTEMPTS = 7
private const val MAX_DELAY_IN_MS = 30000L
