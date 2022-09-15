package org.orbitmvi.orbit

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import org.orbitmvi.orbit.idling.IdlingResource
import org.orbitmvi.orbit.idling.NoopIdlingResource

public data class RealSettings(
    public val sideEffectBufferSize: Int = Channel.BUFFERED,
    public val idlingRegistry: IdlingResource = NoopIdlingResource(),
    public val eventLoopDispatcher: CoroutineDispatcher = Dispatchers.Default,
    public val intentLaunchingDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    public val exceptionHandler: CoroutineExceptionHandler? = null,
    public val repeatOnSubscribedStopTimeout: Long = 100L
)

public class SettingsBuilder {
    internal var settings = RealSettings()

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
}

@Suppress("DEPRECATION")
internal fun Container.Settings.toRealSettings() = RealSettings(
    sideEffectBufferSize = sideEffectBufferSize,
    idlingRegistry = idlingRegistry,
    eventLoopDispatcher = intentDispatcher,
    intentLaunchingDispatcher = intentDispatcher,
    exceptionHandler = exceptionHandler,
    repeatOnSubscribedStopTimeout = repeatOnSubscribedStopTimeout
)
