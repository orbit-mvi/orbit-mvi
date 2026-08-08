/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.orbitmvi.orbit.test

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Captures exceptions thrown by intents during a test.
 *
 * When neither a [TestSettings.exceptionHandlerOverride] nor a container-level
 * [org.orbitmvi.orbit.RealSettings.exceptionHandler] is supplied, this installs a default handler that both stores the
 * exception (re-thrown after the `validate` block for tests that never await) and forwards it to [runFailingFast], which
 * fails the test immediately with the real exception instead of letting an `awaitItem`/`awaitState` time out.
 */
internal class OrbitTestExceptionCatcher {
    private val exceptionChannel = Channel<Throwable>(Channel.UNLIMITED)

    var caughtException: Throwable? = null
        private set

    /**
     * Resolves the [CoroutineExceptionHandler] to use for the test. An explicit [TestSettings.exceptionHandlerOverride]
     * or [containerExceptionHandler] takes precedence and preserves existing behaviour; otherwise the fail-fast default
     * handler is installed.
     */
    fun resolveHandler(
        settings: TestSettings,
        containerExceptionHandler: CoroutineExceptionHandler?,
    ): CoroutineExceptionHandler =
        settings.exceptionHandlerOverride
            ?: containerExceptionHandler
            ?: CoroutineExceptionHandler { _, exception ->
                if (exception !is CancellationException) {
                    caughtException = exception
                    exceptionChannel.trySend(exception)
                }
            }

    /**
     * Runs [block] alongside a watcher that rethrows the first exception captured by the default handler. When an intent
     * throws while the test is suspended (e.g. inside `awaitState`), the watcher fails the enclosing scope with the real
     * exception so the test fails fast with the actual cause rather than a Turbine timeout.
     */
    suspend fun <T> runFailingFast(block: suspend () -> T): T = coroutineScope {
        val watcher = launch {
            throw exceptionChannel.receive()
        }
        try {
            block()
        } finally {
            watcher.cancel()
        }
    }
}
