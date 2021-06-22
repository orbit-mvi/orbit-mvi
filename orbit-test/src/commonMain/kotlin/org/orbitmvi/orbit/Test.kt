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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import org.orbitmvi.orbit.internal.TestContainerDecorator
import org.orbitmvi.orbit.internal.TestingStrategy
import kotlin.test.assertEquals

/**
 *  Switches your [ContainerHost] into test mode. Allows you to isolate the flow to the next one
 *  called (default) i.e. method calls on the container beyond the first will be registered but not
 *  actually execute. This allows you to assert any loopbacks
 *  while keeping your test isolated to the flow you are testing, thus avoiding overly complex
 *  tests with many states/side effects being emitted.
 *
 * @param initialState The state to initialize the test container with
 * @param isolateFlow Whether the flow should be isolated
 * @param runOnCreate Whether to run the container's create lambda
 * @param settings You can substitute the [Container.Settings]
 * @return Your [ContainerHost] in test mode.
 */
public fun <STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>> T.test(
    initialState: STATE,
    isolateFlow: Boolean = true,
    runOnCreate: Boolean = false
): SuspendingTestContainerHost<STATE, SIDE_EFFECT, T> {
    container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Suspending
    )

    return SuspendingTestContainerHost(this, initialState, isolateFlow, runOnCreate)
}

/**
 *  Switches your [ContainerHost] into test mode. Allows you to isolate the flow to the next one
 *  called (default) i.e. method calls on the container beyond the first will be registered but not
 *  actually execute. This allows you to assert any loopbacks
 *  while keeping your test isolated to the flow you are testing, thus avoiding overly complex
 *  tests with many states/side effects being emitted.
 *
 * @param initialState The state to initialize the test container with
 * @param isolateFlow Whether the flow should be isolated
 * @param runOnCreate Whether to run the container's create lambda
 * @param settings You can substitute the [Container.Settings]
 * @return Your [ContainerHost] in test mode.
 */
public fun <STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>> T.liveTest(
    initialState: STATE,
    runOnCreate: Boolean = false,
    settings: Container.Settings = container.settings.copy(orbitDispatcher = Dispatchers.Unconfined)
): RegularTestContainerHost<STATE, SIDE_EFFECT, T> {
    container.findTestContainer().test(
        initialState = initialState,
        strategy = TestingStrategy.Live(settings)
    )

    return RegularTestContainerHost(this, initialState, runOnCreate)
}

private fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.findOnCreate(): (STATE) -> Unit {
    return (this as? LazyCreateContainerDecorator<STATE, SIDE_EFFECT>)?.onCreate
        ?: (this as? ContainerDecorator<STATE, SIDE_EFFECT>)?.actual?.findOnCreate()
        ?: {}
}

private fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.findTestContainer(): TestContainerDecorator<STATE, SIDE_EFFECT> {
    return (this as? TestContainerDecorator<STATE, SIDE_EFFECT>)
        ?: (this as? ContainerDecorator<STATE, SIDE_EFFECT>)?.actual?.findTestContainer()
        ?: throw IllegalStateException("No TestContainerDecorator found!")
}


public class SuspendingTestContainerHost<STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: T,
    initialState: STATE,
    private val isolateFlow: Boolean,
    runOnCreate: Boolean
) : TestContainerHost<STATE, SIDE_EFFECT, T>(actual, initialState, runOnCreate) {

    public suspend fun testIntent(action: T.() -> Unit) {
        actual.suspendingIntent { action() }
    }

    @Suppress("EXPERIMENTAL_COROUTINES_API")
    private suspend fun <STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>> T.suspendingIntent(block: T.() -> Unit) {
        val testContainer = container.findTestContainer()

        this.block()

        val firstFlow = withTimeout(1000) {
            testContainer.savedFlows.receive()
        }

        coroutineScope {
            firstFlow()
            while (true) {
                when (val flow = testContainer.savedFlows.poll()) {
                    null -> break
                    else -> if (!isolateFlow) {
                        flow()
                    }
                }
            }
        }
    }
}

public class RegularTestContainerHost<STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: T,
    initialState: STATE,
    runOnCreate: Boolean
) : TestContainerHost<STATE, SIDE_EFFECT, T>(actual, initialState, runOnCreate) {

    public fun testIntent(action: T.() -> Unit) {
        actual.action()
    }
}

public sealed class TestContainerHost<STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: T,
    initialState: STATE,
    runOnCreate: Boolean
) {
    public val stateObserver: TestFlowObserver<STATE> = actual.container.stateFlow.test()
    public val sideEffectObserver: TestFlowObserver<SIDE_EFFECT> = actual.container.sideEffectFlow.test()

    init {
        if (runOnCreate) {
            runBlocking {
                actual.container.findOnCreate().invoke(initialState)
            }
        }
    }

    /**
     * Perform assertions on your [ContainerHost].
     *
     * Specifying all expected states and posted side effects is obligatory, i.e. you cannot do just a
     * partial assertion. Loopback tests are optional.
     *
     * @param block The block containing assertions for your [ContainerHost].
     */
    public fun assert(
        initialState: STATE,
        timeoutMillis: Long = 5000L,
        block: OrbitVerification<STATE, SIDE_EFFECT>.() -> Unit = {}
    ) {
        val verification = OrbitVerification<STATE, SIDE_EFFECT>()
            .apply(block)

        @Suppress("UNCHECKED_CAST")

        // In blocking mode (dispatchers set to unconfined)
//    if (
//        testFixtures.settings.orbitDispatcher != Dispatchers.Unconfined &&
//        testFixtures.settings.backgroundDispatcher != Dispatchers.Unconfined
//    ) {
        // With non-blocking mode await for expected states
        val stateJob = GlobalScope.launch {
            stateObserver.awaitCountSuspending(verification.expectedStateChanges.size + 1, timeoutMillis)
        }
        val sideEffectJob = GlobalScope.launch {
            sideEffectObserver.awaitCountSuspending(verification.expectedSideEffects.size, timeoutMillis)
        }
        runBlocking {
            joinAll(
                stateJob,
                sideEffectJob
            )
//        }
        }

        println(stateObserver.values)

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
