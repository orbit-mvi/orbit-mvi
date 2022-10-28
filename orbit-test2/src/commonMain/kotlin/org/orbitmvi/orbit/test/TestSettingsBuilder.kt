package org.orbitmvi.orbit.test

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.orbitmvi.orbit.RealSettings

@OptIn(ExperimentalCoroutinesApi::class)
public class TestSettingsBuilder internal constructor(
    private var settings: RealSettings
) {
    public constructor() : this(
        UnconfinedTestDispatcher().let {
            RealSettings(
                eventLoopDispatcher = it,
                intentLaunchingDispatcher = it
            )
        }
    )

    public var exceptionHandler: CoroutineExceptionHandler?
        get() = settings.exceptionHandler
        public set(value) {
            settings = settings.copy(exceptionHandler = value)
        }

    public var isolateFlow: Boolean = false

    public fun build(): RealSettings = settings
}

