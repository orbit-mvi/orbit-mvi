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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.internal.TestingStrategy

/**
 *  Puts your [ContainerHost] into live test mode. This mode uses a real Orbit container.
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Replaces the [Container.Settings] for this test
 * @return A live test wrapper around [ContainerHost].
 */
@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)
@OrbitExperimental
public suspend fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.test(
    testScope: TestScope,
    initialState: STATE? = null,
    settings: LiveTestSettings = LiveTestSettings(),
    validate: suspend TestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
) {
    val containerHost = this
    val testDispatcher = testScope.backgroundScope.coroutineContext[CoroutineDispatcher.Key]

    container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Live(settings.toRealSettings(testDispatcher)),
        testScope = testScope.backgroundScope
    )
    mergedFlow().test {
        TestContainerHost(
            containerHost,
            initialState,
            this
        ).apply {
            validate(this)
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun LiveTestSettings.toRealSettings(testDispatcher: CoroutineDispatcher?): RealSettings {
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
