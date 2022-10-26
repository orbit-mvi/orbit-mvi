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

package org.orbitmvi.orbit.test

import app.cash.turbine.test
import app.cash.turbine.testIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.internal.TestingStrategy

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
public suspend fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.turbineTest(
    initialState: STATE? = null,
    buildSettings: TestSettingsBuilder2.() -> Unit = {},
    validate: suspend SuspendingTestContainerHost2<STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
) {
    val settingsBuilder = TestSettingsBuilder2(container.settings).apply(buildSettings)
    container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Suspending(settingsBuilder.build())
    )

    mergedFlow().test {
        SuspendingTestContainerHost2(
            this@turbineTest,
            initialState,
            settingsBuilder.isolateFlow,
            this
        ).apply {
            validate(this)
        }
    }
}

/**
 *  Puts your [ContainerHost] into live test mode. This mode uses a real Orbit container.
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Replaces the [Container.Settings] for this test
 * @return A live test wrapper around [ContainerHost].
 */
public suspend fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.turbineLiveTest(
    initialState: STATE? = null,
    buildSettings: LiveTestSettingsBuilder2.() -> Unit = {},
    validate: suspend RegularTestContainerHost2<STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
) {
    val settingsBuilder = LiveTestSettingsBuilder2(container.settings).apply(buildSettings)
    container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Live(settingsBuilder.build())
    )
    mergedFlow().test {
        RegularTestContainerHost2(
            this@turbineLiveTest,
            initialState,
            this
        ).apply {
            validate(this)
        }
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
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.turbineTestIn(
    coroutineScope: CoroutineScope,
    initialState: STATE? = null,
    buildSettings: TestSettingsBuilder2.() -> Unit = {}
): SuspendingTestContainerHost2<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    val settingsBuilder = TestSettingsBuilder2(container.settings).apply(buildSettings)

    return container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Suspending(settingsBuilder.build())
    )
        .let {
            mergedFlow().testIn(coroutineScope)
        }
        .let {
            SuspendingTestContainerHost2(this, initialState, settingsBuilder.isolateFlow, it)
        }
}

/**
 *  Puts your [ContainerHost] into live test mode. This mode uses a real Orbit container.
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Replaces the [Container.Settings] for this test
 * @return A live test wrapper around [ContainerHost].
 */
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.turbineLiveTestIn(
    coroutineScope: CoroutineScope,
    initialState: STATE? = null,
    buildSettings: LiveTestSettingsBuilder2.() -> Unit = {}
): RegularTestContainerHost2<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    val settingsBuilder = LiveTestSettingsBuilder2(container.settings).apply(buildSettings)
    return container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Live(settingsBuilder.build())
    )
        .let {
            mergedFlow().testIn(coroutineScope)
        }
        .let {
            RegularTestContainerHost2(this, initialState, it)
        }
}

private fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.mergedFlow() =
    merge(
        container.stateFlow
            .map<STATE, Item<STATE, SIDE_EFFECT>> { Item.StateItem(it) },
        container.sideEffectFlow
            .map<SIDE_EFFECT, Item<STATE, SIDE_EFFECT>> { Item.SideEffectItem(it) }
    )
