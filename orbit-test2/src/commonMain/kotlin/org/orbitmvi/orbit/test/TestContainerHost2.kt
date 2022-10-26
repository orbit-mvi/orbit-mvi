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
import app.cash.turbine.testIn
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import org.orbitmvi.orbit.internal.TestContainerDecorator

public abstract class BaseTestContainerHost2<STATE : Any, SIDE_EFFECT : Any>(
    coroutineScope: CoroutineScope,
    private val actual: ContainerHost<STATE, SIDE_EFFECT>,
    initialState: STATE?,
) : OrbitTestContext<STATE, SIDE_EFFECT> {
    private val emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>> = merge(
        actual.container.stateFlow
            .map<STATE, Item<STATE, SIDE_EFFECT>> { Item.StateItem(it) },
        actual.container.sideEffectFlow
            .map<SIDE_EFFECT, Item<STATE, SIDE_EFFECT>> { Item.SideEffectItem(it) }
    ).testIn(scope = coroutineScope, timeout = 1.seconds)

    private lateinit var _previousState: STATE

    private var upcomingState: STATE? = null

    public override val previousState: STATE
        get() = _previousState

    protected val resolvedInitialState: STATE by lazy { initialState ?: actual.container.findTestContainer().originalInitialState }

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

public class SuspendingTestContainerHost2<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST: ContainerHost<STATE, SIDE_EFFECT>>(
    coroutineScope: CoroutineScope,
    private val actual: CONTAINER_HOST,
    initialState: STATE?,
    private val isolateFlow: Boolean,
) : BaseTestContainerHost2<STATE, SIDE_EFFECT>(
    coroutineScope, actual, initialState
) {

    private val onCreateAllowed = atomic(true)

    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    public suspend fun runOnCreate() {
        if (!onCreateAllowed.compareAndSet(expect = true, update = false)) {
            error("runOnCreate should only be invoked once and before any testIntent call")
        }
        actual.container.findOnCreate().invoke(resolvedInitialState)
        actual.suspendingIntent(shouldIsolateFlow = false) {}
    }

    /**
     * Invoke an intent on the [ContainerHost] under test as a suspending function.
     */
    public suspend fun invokeIntent(action: CONTAINER_HOST.() -> Unit) {
        onCreateAllowed.lazySet(false)
        actual.suspendingIntent(isolateFlow, action)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun CONTAINER_HOST.suspendingIntent(
        shouldIsolateFlow: Boolean,
        block: CONTAINER_HOST.() -> Unit
    ) {
        val testContainer = container.findTestContainer()

        this.block() // Invoke the Intent

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

public class RegularTestContainerHost2<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST: ContainerHost<STATE, SIDE_EFFECT>>(
    coroutineScope: CoroutineScope,
    private val actual: CONTAINER_HOST,
    initialState: STATE?
) : BaseTestContainerHost2<STATE, SIDE_EFFECT>(
    coroutineScope, actual, initialState
) {

    private val onCreateAllowed = atomic(true)

    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    public fun runOnCreate() {
        if (!onCreateAllowed.compareAndSet(expect = true, update = false)) {
            error("runOnCreate should only be invoked once and before any testIntent call")
        }

        actual.container.findOnCreate().invoke(resolvedInitialState)
    }

    /**
     * Invoke an intent on the [ContainerHost] under test.
     */
    public fun invokeIntent(action: CONTAINER_HOST.() -> Unit) {
        onCreateAllowed.lazySet(false)
        actual.action()
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
