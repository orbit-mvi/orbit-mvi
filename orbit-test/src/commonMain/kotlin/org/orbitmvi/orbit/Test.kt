/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit

import kotlinx.coroutines.Dispatchers
import org.orbitmvi.orbit.internal.TestContainerDecorator
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
 * @param isolateFlow Whether the intent should be isolated
 * @param settings Replaces the [Container.Settings] for this test
 * @return A suspending test wrapper around [ContainerHost].
 */
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.test(
    initialState: STATE? = null,
    isolateFlow: Boolean = true,
    settings: Container.Settings = container.settings
): SuspendingTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    return container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Suspending(settings)
    ).let {
        SuspendingTestContainerHost(this, initialState, isolateFlow)
    }
}

/**
 *  Puts your [ContainerHost] into live test mode. This mode uses a real Orbit container with
 *  some basic test settings.
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Replaces the [Container.Settings] for this test
 * @return A live test wrapper around [ContainerHost].
 */
public fun <STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>> CONTAINER_HOST.liveTest(
    initialState: STATE? = null,
    settings: Container.Settings = container.settings.copy(intentDispatcher = Dispatchers.Unconfined)
): RegularTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
    return container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Live(settings)
    )
        .let {
            RegularTestContainerHost(this, initialState)
        }
}

internal fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.findTestContainer(): TestContainerDecorator<STATE, SIDE_EFFECT> {
    return (this as? TestContainerDecorator<STATE, SIDE_EFFECT>)
        ?: (this as? ContainerDecorator<STATE, SIDE_EFFECT>)?.actual?.findTestContainer()
        ?: throw IllegalStateException("No TestContainerDecorator found!")
}
