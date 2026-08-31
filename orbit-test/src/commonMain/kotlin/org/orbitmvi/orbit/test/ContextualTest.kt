/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.RealSettings
import kotlin.time.Duration

/**
 *  Run tests on your [OrbitContainerHost]. This mode uses a real Orbit container, but the container's [CoroutineDispatcher] is set to the
 *  [TestScope]'s background dispatcher.
 *
 *  The [TestScope] is taken from the enclosing context, typically the scope defined by kotlin's [runTest]. If you need the
 *  [OrbitContainer] to run in a different scope - e.g. to control virtual time and avoid delay skipping - use the overload taking an
 *  explicit `testScope` parameter instead.
 *
 *  During a test, all of the emitted states and side effects must be consumed - otherwise the test fails. See [OrbitTestContext].
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Use this to set overrides for some of the container's [RealSettings] for this test.
 * @param validate Perform your test within this block. See [OrbitTestContext].
 */
@Suppress("MaxLineLength")
context(testScope: TestScope)
public suspend fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : OrbitContainerHost<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> CONTAINER_HOST.testWithInternalState(
    initialState: INTERNAL_STATE? = null,
    timeout: Duration? = null,
    settings: TestSettings = TestSettings(),
    validate:
    suspend OrbitScopedTestContextInternal<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
): Unit = testWithInternalState(testScope, initialState, timeout, settings, validate)

/**
 *  Run tests on your [OrbitContainerHost]. This mode uses a real Orbit container, but the container's [CoroutineDispatcher] is set to the
 *  [TestScope]'s background dispatcher.
 *
 *  The [TestScope] is taken from the enclosing context, typically the scope defined by kotlin's [runTest]. If you need the
 *  [OrbitContainer] to run in a different scope - e.g. to control virtual time and avoid delay skipping - use the overload taking an
 *  explicit `testScope` parameter instead.
 *
 *  During a test, all of the emitted states and side effects must be consumed - otherwise the test fails. See [OrbitTestContext].
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Use this to set overrides for some of the container's [RealSettings] for this test.
 * @param validate Perform your test within this block. See [OrbitTestContext].
 */
@Suppress("MaxLineLength")
context(testScope: TestScope)
public suspend fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : OrbitContainerHost<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> CONTAINER_HOST.testWithExternalState(
    initialState: INTERNAL_STATE? = null,
    timeout: Duration? = null,
    settings: TestSettings = TestSettings(),
    validate:
    suspend OrbitScopedTestContextExternal<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
): Unit = testWithExternalState(testScope, initialState, timeout, settings, validate)

/**
 *  Run tests on your [OrbitContainerHost]. This mode uses a real Orbit container, but the container's [CoroutineDispatcher] is set to the
 *  [TestScope]'s background dispatcher.
 *
 *  The [TestScope] is taken from the enclosing context, typically the scope defined by kotlin's [runTest]. If you need the
 *  [OrbitContainer] to run in a different scope - e.g. to control virtual time and avoid delay skipping - use the overload taking an
 *  explicit `testScope` parameter instead.
 *
 *  During a test, all of the emitted states and side effects must be consumed - otherwise the test fails. See [OrbitTestContext].
 *
 * @param initialState The state to initialize the test container with. Omit this parameter to use the real initial state of the container.
 * @param settings Use this to set overrides for some of the container's [RealSettings] for this test.
 * @param validate Perform your test within this block. See [OrbitTestContext].
 */
@Suppress("MaxLineLength")
context(testScope: TestScope)
public suspend fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : OrbitContainerHost<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>> CONTAINER_HOST.testWithInternalAndExternalState(
    initialState: INTERNAL_STATE? = null,
    timeout: Duration? = null,
    settings: TestSettings = TestSettings(),
    validate:
    suspend OrbitScopedTestContextInternalAndExternal<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT, CONTAINER_HOST>.() -> Unit
): Unit = testWithInternalAndExternalState(testScope, initialState, timeout, settings, validate)
