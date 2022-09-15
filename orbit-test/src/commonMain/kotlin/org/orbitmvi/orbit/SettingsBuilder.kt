package org.orbitmvi.orbit

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.orbitmvi.orbit.idling.IdlingResource

@OptIn(ExperimentalCoroutinesApi::class)
public class TestSettingsBuilder(
    internal var settings: RealSettings
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
}

@OptIn(ExperimentalCoroutinesApi::class)
public class LiveTestSettingsBuilder(
    internal var settings: RealSettings
) {
    public constructor() : this(
        UnconfinedTestDispatcher().let {
            RealSettings(
                eventLoopDispatcher = it,
                intentLaunchingDispatcher = it
            )
        }
    )

    public var dispatcher: CoroutineDispatcher
        get() = settings.eventLoopDispatcher
        public set(value) {
            settings = settings.copy(
                eventLoopDispatcher = value,
                intentLaunchingDispatcher = value
            )
        }

    public var idlingRegistry: IdlingResource
        get() = settings.idlingRegistry
        public set(value) {
            settings = settings.copy(idlingRegistry = value)
        }

    public var exceptionHandler: CoroutineExceptionHandler?
        get() = settings.exceptionHandler
        public set(value) {
            settings = settings.copy(exceptionHandler = value)
        }

    public var repeatOnSubscribedStopTimeout: Long
        get() = settings.repeatOnSubscribedStopTimeout
        public set(value) {
            settings = settings.copy(repeatOnSubscribedStopTimeout = value)
        }
}

@Suppress("DEPRECATION")
internal fun Container.Settings.toRealSettings() = RealSettings(
    sideEffectBufferSize = sideEffectBufferSize,
    idlingRegistry = idlingRegistry,
    eventLoopDispatcher = intentDispatcher,
    exceptionHandler = exceptionHandler,
    repeatOnSubscribedStopTimeout = repeatOnSubscribedStopTimeout
)
