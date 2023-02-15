package org.orbitmvi.orbit.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import org.orbitmvi.orbit.Container

public data class TestSettings(
    /**
     * Set this to override the [Container]'s [CoroutineDispatcher]s for this test
     */
    val dispatcherOverride: CoroutineDispatcher? = null,
    /**
     * Set this to override the [Container]'s [CoroutineExceptionHandler]s for this test
     */
    val exceptionHandlerOverride: CoroutineExceptionHandler? = null,
)
