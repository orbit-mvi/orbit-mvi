/*
 * Copyright 2023-2025 Mikołaj Leszczyński & Appmattus Limited
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

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.StandardTestDispatcher
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
public suspend fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.test(
    testScope: TestScope,
    initialState: STATE? = null,
    timeout: Duration? = null,
    settings: TestSettings = TestSettings(),
    validate: suspend OrbitTestContext<STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
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

    val resolvedInitialState: STATE = initialState ?: containerHost.container.findTestContainer().originalInitialState

    merge(
        container.stateFlow.map<STATE, Item<STATE, SIDE_EFFECT>> { Item.StateItem(it) },
        container.sideEffectFlow.map<SIDE_EFFECT, Item<STATE, SIDE_EFFECT>> { Item.SideEffectItem(it) }
    ).test(timeout = timeout) {
        OrbitTestContext(
            containerHost,
            resolvedInitialState,
            this,
            settings
        ).apply {
            if (settings.autoCheckInitialState) {
                assertEquals(resolvedInitialState, awaitState())
            }
            validate(this)
            caughtException?.let { throw it }
            withAppropriateTimeout(timeout ?: 1.seconds) {
                container.findTestContainer().joinIntents()
            }
        }
    }
}

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
public suspend fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> CONTAINER_HOST.test(
    testScope: TestScope,
    initialState: INTERNAL_STATE? = null,
    timeout: Duration? = null,
    settings: TestSettings = TestSettings(),
    validate:
    suspend OrbitTestContextWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
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
        if (settings.awaitState != AwaitState.EXTERNAL_ONLY) {
            add(
                container.stateFlow
                    .map<INTERNAL_STATE, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { InternalStateItem(it) }
            )
        }
        if (settings.awaitState != AwaitState.INTERNAL_ONLY) {
            add(
                container.externalStateFlow
                    .map<EXTERNAL_STATE, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { ExternalStateItem(it) }
            )
        }

        add(
            container.sideEffectFlow
                .map<SIDE_EFFECT, ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> { SideEffectItem(it) }
        )
    }.merge().test(timeout = timeout) {
        OrbitTestContextWithExternalState(
            containerHost,
            resolvedInitialState,
            this as ReceiveTurbine<ItemWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>>,
            settings
        ).apply {
            if (settings.autoCheckInitialState) {
                if (settings.awaitState != AwaitState.EXTERNAL_ONLY) {
                    assertEquals(resolvedInitialState, awaitInternalState())
                }
                if (settings.awaitState != AwaitState.INTERNAL_ONLY) {
                    assertEquals(container.mapToExternalState(resolvedInitialState), awaitExternalState())
                }
            }
            validate(this)
            caughtException?.let { throw it }
            withAppropriateTimeout(timeout ?: 1.seconds) {
                container.findTestContainer().joinIntents()
            }
        }
    }
}

private fun createRealSettings(testDispatcher: CoroutineDispatcher?, testExceptionHandler: CoroutineExceptionHandler?): RealSettings {
    val dispatcher = testDispatcher ?: StandardTestDispatcher()

    return RealSettings(
        eventLoopDispatcher = dispatcher,
        intentLaunchingDispatcher = dispatcher,
        exceptionHandler = testExceptionHandler,
        repeatOnSubscribedStopTimeout = 0L
    )
}
