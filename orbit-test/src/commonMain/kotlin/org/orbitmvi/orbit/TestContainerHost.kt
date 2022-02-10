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

package org.orbitmvi.orbit

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals

public sealed class TestContainerHost<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    actual: CONTAINER_HOST
) {
    public val stateObserver: TestFlowObserver<STATE> = actual.container.stateFlow.test()
    public val sideEffectObserver: TestFlowObserver<SIDE_EFFECT> = actual.container.sideEffectFlow.test()

    protected abstract fun awaitForEmissions(verification: OrbitVerification<STATE, SIDE_EFFECT>, timeoutMillis: Long)

    /**
     * Perform assertions on your [ContainerHost].
     *
     * Specifying all expected states and posted side effects is obligatory, i.e. you cannot do just a
     * partial assertion. Loopback tests are optional.
     *
     * @param initialState The initial container state to assert
     * @param timeoutMillis How long the assert call should wait for emissions before timing out
     * @param block The block containing assertions for your [ContainerHost]
     */
    public fun assert(
        initialState: STATE,
        timeoutMillis: Long = 1000L,
        block: OrbitVerification<STATE, SIDE_EFFECT>.() -> Unit = {}
    ) {
        val verification = OrbitVerification<STATE, SIDE_EFFECT>()
            .apply(block)

        awaitForEmissions(verification, timeoutMillis)

        // sanity check the initial state
        assertEquals(
            initialState,
            stateObserver.values.firstOrNull()
        )

        assertStatesInOrder(
            stateObserver.values.drop(1),
            verification.expectedStateChanges,
            initialState
        )

        assertEquals(
            verification.expectedSideEffects,
            sideEffectObserver.values
        )
    }
}

public class SuspendingTestContainerHost<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: CONTAINER_HOST,
    private val initialState: STATE?,
    private val isolateFlow: Boolean,
) : TestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST>(actual) {

    private val onCreateAllowed = atomic(true)

    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    public suspend fun runOnCreate(): SuspendingTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
        if (!onCreateAllowed.compareAndSet(expect = true, update = false)) {
            throw IllegalStateException("runOnCreate should only be invoked once and before any testIntent call")
        }
        actual.container.findOnCreate().invoke(initialState ?: actual.container.findTestContainer().originalInitialState)
        actual.suspendingIntent(shouldIsolateFlow = false) {}
        return this
    }

    /**
     * Invoke an intent on the [ContainerHost] under test as a suspending function.
     */
    public suspend fun testIntent(action: CONTAINER_HOST.() -> Unit): SuspendingTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
        onCreateAllowed.lazySet(false)
        actual.suspendingIntent(isolateFlow, action)
        return this
    }

    private suspend fun <STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>> T.suspendingIntent(
        shouldIsolateFlow: Boolean,
        block: T.() -> Unit
    ) {
        val testContainer = container.findTestContainer()

        this.block() // Invoke the Intent

        var firstIntentExecuted = false
        @Suppress("EXPERIMENTAL_API_USAGE")
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

    // Nothing to do here, since intents are executed entirely as suspending
    protected override fun awaitForEmissions(verification: OrbitVerification<STATE, SIDE_EFFECT>, timeoutMillis: Long): Unit = Unit
}

public class RegularTestContainerHost<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: CONTAINER_HOST,
    private val initialState: STATE?,
) : TestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST>(actual) {

    private val onCreateAllowed = atomic(true)

    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    public fun runOnCreate(): RegularTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
        if (!onCreateAllowed.compareAndSet(expect = true, update = false)) {
            throw IllegalStateException("runOnCreate should only be invoked once and before any testIntent call")
        }

        actual.container.findOnCreate().invoke(initialState ?: actual.container.findTestContainer().originalInitialState)
        return this
    }

    /**
     * Invoke an intent on the [ContainerHost] under test.
     */
    public fun testIntent(action: CONTAINER_HOST.() -> Unit): RegularTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST> {
        onCreateAllowed.lazySet(false)
        actual.action()
        return this
    }

    protected override fun awaitForEmissions(verification: OrbitVerification<STATE, SIDE_EFFECT>, timeoutMillis: Long) {
        // With non-blocking mode await for expected states
        runBlocking {
            coroutineScope {
                joinAll(
                    launch {
                        stateObserver.awaitCountSuspending(verification.expectedStateChanges.size + 1, timeoutMillis)
                    },
                    launch {
                        sideEffectObserver.awaitCountSuspending(verification.expectedSideEffects.size, timeoutMillis)
                    }
                )
            }
        }
    }
}

private fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.findOnCreate(): (STATE) -> Unit {
    return (this as? LazyCreateContainerDecorator<STATE, SIDE_EFFECT>)?.onCreate
        ?: (this as? ContainerDecorator<STATE, SIDE_EFFECT>)?.actual?.findOnCreate()
        ?: {}
}
