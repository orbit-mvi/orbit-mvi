package org.orbitmvi.orbit

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
public class TestSettingsBuilder internal constructor(
    private var settings: RealSettings
) {
    public var exceptionHandler: CoroutineExceptionHandler?
        get() = settings.exceptionHandler
        public set(value) {
            settings = settings.copy(exceptionHandler = value)
        }

    public var isolateFlow: Boolean = true

    public fun build(): RealSettings = settings
}

@OptIn(ExperimentalCoroutinesApi::class)
public class LiveTestSettingsBuilder internal constructor(
    private var settings: RealSettings
) {
    public var dispatcher: CoroutineDispatcher
        get() = settings.eventLoopDispatcher
        public set(value) {
            settings = settings.copy(
                eventLoopDispatcher = value,
                intentLaunchingDispatcher = value
            )
        }

    public var exceptionHandler: CoroutineExceptionHandler?
        get() = settings.exceptionHandler
        public set(value) {
            settings = settings.copy(exceptionHandler = value)
        }

    public fun build(): RealSettings = settings
}
