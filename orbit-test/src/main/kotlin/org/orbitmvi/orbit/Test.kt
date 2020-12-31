/*
 * Copyright 2021 Mikolaj Leszczynski & Matthew Dolan
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.orbitmvi.orbit

import org.orbitmvi.orbit.internal.LazyCreateContainerDecorator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.WeakHashMap
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
 * @return Your [ContainerHost] in test mode.
 */
public fun <STATE : Any, SIDE_EFFECT : Any, T : ContainerHost<STATE, SIDE_EFFECT>> T.test(
    initialState: STATE,
    isolateFlow: Boolean = true,
    runOnCreate: Boolean = false,
    blocking: Boolean = true
): T {

    val onCreate = container.findOnCreate()

    @Suppress("EXPERIMENTAL_API_USAGE")
    val testContainer = TestContainer<STATE, SIDE_EFFECT>(
        initialState,
        isolateFlow,
        blocking
    )

    this.javaClass.declaredFields.firstOrNull { it.name == "container" }?.apply {
        isAccessible = true
        set(this@test, testContainer)
    }

    TestHarness.FIXTURES[this] = TestFixtures(
        container.stateFlow.test(),
        container.sideEffectFlow.test(),
        blocking
    )

    if (runOnCreate) {
        onCreate(initialState)
    }

    return this
}

private fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.findOnCreate(): (STATE) -> Unit {
    return (this as? LazyCreateContainerDecorator<STATE, SIDE_EFFECT>)?.onCreate
        ?: (this as? ContainerDecorator<STATE, SIDE_EFFECT>)?.actual?.findOnCreate()
        ?: {}
}

/**
 * Perform assertions on your [ContainerHost].
 *
 * Specifying all expected states and posted side effects is obligatory, i.e. you cannot do just a
 * partial assertion. Loopback tests are optional.
 *
 * @param block The block containing assertions for your [ContainerHost].
 */
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.assert(
    initialState: STATE,
    timeoutMillis: Long = 5000L,
    block: OrbitVerification<STATE, SIDE_EFFECT>.() -> Unit = {}
) {
    val verification = OrbitVerification<STATE, SIDE_EFFECT>()
        .apply(block)

    @Suppress("UNCHECKED_CAST")
    val testFixtures = TestHarness.FIXTURES[this] as TestFixtures<STATE, SIDE_EFFECT>

    if (!testFixtures.blocking) {
        // With non-blocking mode await for expected states
        runBlocking {
            joinAll(
                launch(Dispatchers.IO) {
                    testFixtures.stateObserver.awaitCountSuspending(verification.expectedStateChanges.size + 1, timeoutMillis)
                },
                launch(Dispatchers.IO) {
                    testFixtures.sideEffectObserver.awaitCountSuspending(verification.expectedSideEffects.size, timeoutMillis)
                }
            )
        }
    }

    // sanity check the initial state
    assertEquals(
        initialState,
        testFixtures.stateObserver.values.firstOrNull()
    )

    assertStatesInOrder(
        testFixtures.stateObserver.values.drop(1),
        verification.expectedStateChanges,
        initialState
    )

    assertEquals(
        verification.expectedSideEffects,
        testFixtures.sideEffectObserver.values
    )
}

private class TestFixtures<STATE : Any, SIDE_EFFECT : Any>(
    val stateObserver: TestFlowObserver<STATE>,
    val sideEffectObserver: TestFlowObserver<SIDE_EFFECT>,
    val blocking: Boolean
)

private object TestHarness {
    val FIXTURES: MutableMap<ContainerHost<*, *>, TestFixtures<*, *>> = WeakHashMap()
}
