/*
 * Copyright 2021-2022 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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

@file:Suppress("DEPRECATION")

package org.orbitmvi.orbit

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.orbitmvi.orbit.internal.TestingStrategy
import org.orbitmvi.orbit.test.findTestContainer

/**
 *  Puts your [ContainerHost] into suspending test mode. Intents are intercepted and executed as
 *  suspending functions.
 *
 *  Allows you to test your intents in isolation. Only the intent called will actually run.
 *  Method calls on the [ContainerHost] beyond the first will be registered but not
 *  actually execute. This allows you to test your intents in isolation, disabling loopbacks
 *  that might make your tests too complex.
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param isolateFlow Whether the intent should be isolated
 * @param settings Replaces the [Container.Settings] for this test
 * @return A suspending test wrapper around [ContainerHost].
 */
@Deprecated(message = "Use org.orbitmvi.orbit.test.test instead. This will be removed in the future.")
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.test(
    initialState: STATE? = null,
    isolateFlow: Boolean = true,
    settings: Container.Settings
): SuspendingTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    return container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Suspending(settings.toRealSettings()),
        testScope = null
    ).let {
        SuspendingTestContainerHost(this, initialState, isolateFlow)
    }
}

/**
 *  Puts your [ContainerHost] into suspending test mode. Intents are intercepted and executed as
 *  suspending functions.
 *
 *  Allows you to test your intents in isolation. Only the intent called will actually run.
 *  Method calls on the [ContainerHost] beyond the first will be registered but not
 *  actually execute. This allows you to test your intents in isolation, disabling loopbacks
 *  that might make your tests too complex.
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param isolateFlow Whether the intent should be isolated
 * @param buildSettings Builds the [RealSettings] for this test
 * @return A suspending test wrapper around [ContainerHost].
 */
@Deprecated(message = "Use org.orbitmvi.orbit.test.test instead. This will be removed in the future.")
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.test(
    initialState: STATE? = null,
    isolateFlow: Boolean = true,
    buildSettings: TestSettingsBuilder.() -> Unit = {},
): SuspendingTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    val settingsBuilder = TestSettingsBuilder(container.settings).apply(buildSettings)
    return container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Suspending(settingsBuilder.build()),
        testScope = null
    ).let {
        SuspendingTestContainerHost(this, initialState, isolateFlow)
    }
}

/**
 *  Puts your [ContainerHost] into suspending test mode. Intents are intercepted and executed as
 *  suspending functions.
 *
 *  Allows you to test your intents in isolation. Only the intent called will actually run.
 *  Method calls on the [ContainerHost] beyond the first will be registered but not
 *  actually execute. This allows you to test your intents in isolation, disabling loopbacks
 *  that might make your tests too complex.
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param buildSettings Builds the [RealSettings] for this test
 * @return A suspending test wrapper around [ContainerHost].
 */
@Deprecated(message = "Use org.orbitmvi.orbit.test.test instead. This will be removed in the future.")
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.test(
    initialState: STATE? = null,
    buildSettings: TestSettingsBuilder.() -> Unit = {},
): SuspendingTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    val settingsBuilder = TestSettingsBuilder(container.settings).apply(buildSettings)
    return container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Suspending(settingsBuilder.build()),
        testScope = null
    ).let {
        SuspendingTestContainerHost(this, initialState, settingsBuilder.isolateFlow)
    }
}

/**
 *  Puts your [ContainerHost] into live test mode. This mode uses a real Orbit container with
 *  some basic test settings. Orbit dispatchers are overridden with [UnconfinedTestDispatcher] by default.
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param buildSettings Builds the [RealSettings] for this test
 * @return A live test wrapper around [ContainerHost].
 */
@Deprecated(message = "Use org.orbitmvi.orbit.test.test instead. This will be removed in the future.")
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.liveTest(
    initialState: STATE? = null,
    buildSettings: LiveTestSettingsBuilder.() -> Unit = {},
): RegularTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    return container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Live(LiveTestSettingsBuilder(container.settings).apply(buildSettings).build()),
        testScope = null
    )
        .let {
            RegularTestContainerHost(this, initialState)
        }
}

/**
 *  Puts your [ContainerHost] into live test mode. This mode uses a real Orbit container.
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Replaces the [Container.Settings] for this test
 * @return A live test wrapper around [ContainerHost].
 */
@Deprecated(message = "Use org.orbitmvi.orbit.test.test instead. This will be removed in the future.")
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.liveTest(
    initialState: STATE? = null,
    settings: Container.Settings
): RegularTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    return container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Live(settings.toRealSettings()),
        testScope = null
    )
        .let {
            RegularTestContainerHost(this, initialState)
        }
}
