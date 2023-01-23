package org.orbitmvi.orbit.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler

public data class LiveTestSettings(
    val dispatcherOverride: CoroutineDispatcher? = null,
    val exceptionHandlerOverride: CoroutineExceptionHandler? = null,
)
