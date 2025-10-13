package org.orbitmvi.orbit.test

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.ContainerHostWithExternalState
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.test.ItemWithExternalState.ExternalStateItem
import org.orbitmvi.orbit.test.ItemWithExternalState.InternalStateItem
import org.orbitmvi.orbit.test.ItemWithExternalState.SideEffectItem
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 *  Run tests on your [ContainerHost]. This mode uses a real Orbit container, but the container's [CoroutineDispatcher] is set to the
 *  [TestScope]'s background dispatcher.
 *
 *  Typically this is the scope defined by kotlin's [runTest], but you are free to provide your own [TestScope].
 *  This is useful if you wish to e.g. control virtual time to avoid delay skipping.
 *
 *  During a test, all of the emitted states and side effects must be consumed - otherwise the test fails. See [OrbitTestContext].
 *
 * @param testScope The scope in which the [Container] will run.
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Use this to set overrides for some of the container's [RealSettings] for this test.
 * @param validate Perform your test within this block. See [OrbitTestContext].
 */
@OptIn(ExperimentalStdlibApi::class)
@Suppress("MaxLineLength")
public suspend fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> CONTAINER_HOST.testInternalState(
    testScope: TestScope,
    initialState: INTERNAL_STATE? = null,
    timeout: Duration? = null,
    settings: TestSettings = TestSettings(),
    validate: suspend OrbitScopedTestContextInternal<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
) {
    val containerHost = this
    val testDispatcher = settings.dispatcherOverride ?: testScope.backgroundScope.coroutineContext[CoroutineDispatcher.Key]

    var caughtException: Throwable? = null

    val testExceptionHandler = settings.exceptionHandlerOverride
        ?: containerHost.container.settings.exceptionHandler
        ?: CoroutineExceptionHandler { _, exception ->
            if (exception !is CancellationException) {
                caughtException = exception
            }
        }

    container.findTestContainer().test(
        initialState = initialState,
        settings = createRealSettings(testDispatcher, testExceptionHandler),
        testScope = testScope.backgroundScope
    )

    val resolvedInitialState: INTERNAL_STATE =
        initialState ?: containerHost.container.findTestContainer().originalInitialState

    buildList {
        add(
            container.stateFlow
                .map<INTERNAL_STATE, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { InternalStateItem(it) }
        )

        add(
            container.sideEffectFlow
                .map<SIDE_EFFECT, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { SideEffectItem(it) }
        )
    }.merge().test(timeout = timeout) {
        OrbitScopedTestContextInternal(
            containerHost,
            resolvedInitialState,
            this as ReceiveTurbine<ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>>,
            settings
        ).apply {
            if (settings.autoCheckInitialState) {
                assertEquals(resolvedInitialState, awaitInternalState())
            }
            validate(this)
            caughtException?.let { throw it }
            withAppropriateTimeout(timeout ?: 1.seconds) {
                container.findTestContainer().joinIntents()
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Suppress("MaxLineLength")
public suspend fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> CONTAINER_HOST.testExternalState(
    testScope: TestScope,
    initialState: INTERNAL_STATE? = null,
    timeout: Duration? = null,
    settings: TestSettings = TestSettings(),
    validate: suspend OrbitScopedTestContextExternal<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
) {
    val containerHost = this
    val testDispatcher = settings.dispatcherOverride ?: testScope.backgroundScope.coroutineContext[CoroutineDispatcher.Key]

    var caughtException: Throwable? = null

    val testExceptionHandler = settings.exceptionHandlerOverride
        ?: containerHost.container.settings.exceptionHandler
        ?: CoroutineExceptionHandler { _, exception ->
            if (exception !is CancellationException) {
                caughtException = exception
            }
        }

    container.findTestContainer().test(
        initialState = initialState,
        settings = createRealSettings(testDispatcher, testExceptionHandler),
        testScope = testScope.backgroundScope
    )

    val resolvedInitialState: INTERNAL_STATE =
        initialState ?: containerHost.container.findTestContainer().originalInitialState

    buildList {
        add(
            container.externalStateFlow
                .map<EXTERNAL_STATE, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { ExternalStateItem(it) }
        )

        add(
            container.sideEffectFlow
                .map<SIDE_EFFECT, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { SideEffectItem(it) }
        )
    }.merge().test(timeout = timeout) {
        OrbitScopedTestContextExternal(
            containerHost,
            resolvedInitialState,
            this as ReceiveTurbine<ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>>,
            settings
        ).apply {
            if (settings.autoCheckInitialState) {
                assertEquals(container.transformState(resolvedInitialState), awaitExternalState())
            }
            validate(this)
            caughtException?.let { throw it }
            withAppropriateTimeout(timeout ?: 1.seconds) {
                container.findTestContainer().joinIntents()
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Suppress("MaxLineLength")
public suspend fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> CONTAINER_HOST.testInternalAndExternalState(
    testScope: TestScope,
    initialState: INTERNAL_STATE? = null,
    timeout: Duration? = null,
    settings: TestSettings = TestSettings(),
    validate: suspend OrbitScopedTestContextInternalAndExternal<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
) {
    val containerHost = this
    val testDispatcher = settings.dispatcherOverride ?: testScope.backgroundScope.coroutineContext[CoroutineDispatcher.Key]

    var caughtException: Throwable? = null

    val testExceptionHandler = settings.exceptionHandlerOverride
        ?: containerHost.container.settings.exceptionHandler
        ?: CoroutineExceptionHandler { _, exception ->
            if (exception !is CancellationException) {
                caughtException = exception
            }
        }

    container.findTestContainer().test(
        initialState = initialState,
        settings = createRealSettings(testDispatcher, testExceptionHandler),
        testScope = testScope.backgroundScope
    )

    val resolvedInitialState: INTERNAL_STATE =
        initialState ?: containerHost.container.findTestContainer().originalInitialState

    buildList {
        add(
            container.stateFlow
                .map<INTERNAL_STATE, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { InternalStateItem(it) }
        )
        add(
            container.externalStateFlow
                .map<EXTERNAL_STATE, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { ExternalStateItem(it) }
        )
        add(
            container.sideEffectFlow
                .map<SIDE_EFFECT, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { SideEffectItem(it) }
        )
    }.merge().test(timeout = timeout) {
        OrbitScopedTestContextInternalAndExternal(
            containerHost,
            resolvedInitialState,
            this as ReceiveTurbine<ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>>,
            settings
        ).apply {
            if (settings.autoCheckInitialState) {
                assertEquals(resolvedInitialState, awaitInternalState())
                assertEquals(container.transformState(resolvedInitialState), awaitExternalState())
            }
            validate(this)
            caughtException?.let { throw it }
            withAppropriateTimeout(timeout ?: 1.seconds) {
                container.findTestContainer().joinIntents()
            }
        }
    }
}
