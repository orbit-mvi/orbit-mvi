/*
 * Copyright 2021-2022 Mikołaj Leszczyński & Appmattus Limited
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
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import org.orbitmvi.orbit.internal.TestContainerDecorator

public abstract class BaseTestContainerHost2<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: CONTAINER_HOST,
    initialState: STATE?,
    private val emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>>
) : OrbitTestContext<STATE, SIDE_EFFECT> {

    private val onCreateAllowed = atomic(true)

    private lateinit var _previousState: STATE

    private var upcomingState: STATE? = null

    public override val previousState: STATE
        get() = _previousState

    protected val resolvedInitialState: STATE by lazy { initialState ?: actual.container.findTestContainer().originalInitialState }

    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    protected fun runOnCreateInternal() {
        if (!onCreateAllowed.compareAndSet(expect = true, update = false)) {
            error("runOnCreate should only be invoked once and before any testIntent call")
        }

        actual.container.findOnCreate().invoke(resolvedInitialState)
    }

    /**
     * Invoke an intent on the [ContainerHost] under test.
     */
    public fun invokeIntentInternal(action: CONTAINER_HOST.() -> Unit) {
        onCreateAllowed.lazySet(false)
        actual.action()
    }

    override suspend fun awaitItem(): Item<STATE, SIDE_EFFECT> {
        upcomingState?.let { _previousState = it }
        val upcoming = emissions.awaitItem()

        if (upcoming is Item.StateItem) {
            upcomingState = upcoming.value
        }
        return upcoming
    }

    public override suspend fun awaitState(): STATE {
        upcomingState?.let { _previousState = it }
        val item = awaitItem()

        return (item as? Item.StateItem)?.value.also { upcomingState = it } ?: fail("Expected State but got $item")
    }

    public override suspend fun awaitSideEffect(): SIDE_EFFECT {
        val item = awaitItem()

        return (item as? Item.SideEffectItem)?.value ?: fail("Expected Side Effect but got $item")
    }

    override suspend fun cancel() {
        emissions.cancel()
    }

    override suspend fun cancelAndIgnoreRemainingEvents() {
        emissions.cancelAndIgnoreRemainingEvents()
    }

    override suspend fun expectInitialState() {
        assertEquals(resolvedInitialState, awaitState())
    }

    override fun expectMostRecentItem(): Item<STATE, SIDE_EFFECT> {
        return emissions.expectMostRecentItem()
    }

    override suspend fun skipItems(count: Int) {
        emissions.skipItems(count)
    }
}

public class SuspendingTestContainerHost2<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: CONTAINER_HOST,
    initialState: STATE?,
    private val isolateFlow: Boolean,
    emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>>
) : BaseTestContainerHost2<STATE, SIDE_EFFECT, CONTAINER_HOST>(
    actual, initialState, emissions
) {
    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    public suspend fun runOnCreate() {
        super.runOnCreateInternal()
        actual.suspendingIntent(shouldIsolateFlow = false)
    }

    /**
     * Invoke an intent on the [ContainerHost] under test as a suspending function.
     */
    public suspend fun invokeIntent(action: CONTAINER_HOST.() -> Unit) {
        super.invokeIntentInternal(action)
        actual.suspendingIntent(isolateFlow)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun CONTAINER_HOST.suspendingIntent(shouldIsolateFlow: Boolean) {
        val testContainer = container.findTestContainer()

        var firstIntentExecuted = false
        while (!testContainer.savedIntents.isEmpty) {
            val intent = testContainer.savedIntents.receive()
            if (!shouldIsolateFlow || !firstIntentExecuted) {
                firstIntentExecuted = true
                val context = actual.container.settings.exceptionHandler?.plus(SupervisorJob()) ?: EmptyCoroutineContext
                coroutineScope {
                    launch(context) {
                        intent()
                    }
                }.join()
            }
        }
    }
}

public class RegularTestContainerHost2<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    actual: CONTAINER_HOST,
    initialState: STATE?,
    emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>>
) : BaseTestContainerHost2<STATE, SIDE_EFFECT, CONTAINER_HOST>(
    actual, initialState, emissions
) {

    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    public fun runOnCreate() {
        super.runOnCreateInternal()
    }

    /**
     * Invoke an intent on the [ContainerHost] under test.
     */
    public fun invokeIntent(action: CONTAINER_HOST.() -> Unit) {
        super.invokeIntentInternal(action)
    }
}

private fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.findOnCreate(): (STATE) -> Unit {
    return (this as? LazyCreateContainerDecorator<STATE, SIDE_EFFECT>)?.onCreate
        ?: (this as? ContainerDecorator<STATE, SIDE_EFFECT>)?.actual?.findOnCreate()
        ?: {}
}

internal fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.findTestContainer(): TestContainerDecorator<STATE, SIDE_EFFECT> {
    return (this as? TestContainerDecorator<STATE, SIDE_EFFECT>)
        ?: (this as? ContainerDecorator<STATE, SIDE_EFFECT>)?.actual?.findTestContainer()
        ?: error("No TestContainerDecorator found!")
}
