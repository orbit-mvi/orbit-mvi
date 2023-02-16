/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.test

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.internal.TestingStrategy

/**
 *  Run tests on your [ContainerHost]. This mode uses a real Orbit container, but the container's [CoroutineDispatcher] is set to the
 *  [TestScope]'s background dispatcher.
 *
 *  This is a convenience function which uses [runTest] from [kotlinx.coroutines.test] internally to ensure predictable testing behaviour.
 *  Do not use this function if you are already within a [runTest] block. If that's the case, use the overload and pass in the [TestScope]
 *  yourself.
 *
 *  During a test, all of the emitted states and side effects must be consumed - otherwise the test fails. See [OrbitTestContext].
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Use this to set overrides for some of the container's [RealSettings] for this test.
 * @param validate Perform your test within this block. See [OrbitTestContext].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@OrbitExperimental
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.test(
    initialState: STATE? = null,
    settings: TestSettings = TestSettings(),
    validate: suspend OrbitTestContext<STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
) {
    runTest {
        executeWithScope(this, initialState, settings, validate)
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
@OptIn(ExperimentalCoroutinesApi::class)
@OrbitExperimental
public suspend fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.test(
    testScope: TestScope,
    initialState: STATE? = null,
    settings: TestSettings = TestSettings(),
    validate: suspend OrbitTestContext<STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
) {
    executeWithScope(testScope, initialState, settings, validate)
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
private suspend fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.executeWithScope(
    testScope: TestScope,
    initialState: STATE? = null,
    settings: TestSettings = TestSettings(),
    validate: suspend OrbitTestContext<STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
) {
    val containerHost = this
    val testDispatcher = settings.dispatcherOverride ?: testScope.backgroundScope.coroutineContext[CoroutineDispatcher.Key]

    container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Live(settings.toRealSettings(testDispatcher)),
        testScope = testScope.backgroundScope
    )
    mergedFlow().test {
        RealOrbitTestContext(
            containerHost,
            initialState,
            this
        ).apply {
            validate(this)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun TestSettings.toRealSettings(testDispatcher: CoroutineDispatcher?): RealSettings {
    val dispatcher = testDispatcher ?: StandardTestDispatcher()

    return RealSettings(
        eventLoopDispatcher = dispatcher,
        intentLaunchingDispatcher = dispatcher,
        exceptionHandler = exceptionHandlerOverride,
        repeatOnSubscribedStopTimeout = 0L
    )
}

private fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.mergedFlow() =
    merge(
        container.stateFlow
            .map<STATE, Item<STATE, SIDE_EFFECT>> { Item.StateItem(it) },
        container.sideEffectFlow
            .map<SIDE_EFFECT, Item<STATE, SIDE_EFFECT>> { Item.SideEffectItem(it) }
    )
