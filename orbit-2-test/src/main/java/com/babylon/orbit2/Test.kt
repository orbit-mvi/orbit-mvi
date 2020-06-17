/*
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

package com.babylon.orbit2

import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.mockito.Mockito
import java.util.WeakHashMap
import kotlin.test.assertEquals

inline fun <STATE : Any, SIDE_EFFECT : Any, reified T : Host<STATE, SIDE_EFFECT>> T.test(
    initialState: STATE,
    isolateFlow: Boolean = true
): T {

    @Suppress("EXPERIMENTAL_API_USAGE")
    val testContainer = TestContainer<STATE, SIDE_EFFECT>(
        initialState,
        isolateFlow
    )

    val spy = spy(this) {
        on { container }.thenReturn(testContainer)
    }

    TestHarness.fixtures[spy] = TestFixtures(
        initialState,
        spy.container.stateStream.test(),
        spy.container.sideEffectStream.test()
    )

    Mockito.clearInvocations(spy)

    return spy
}

fun <STATE : Any, SIDE_EFFECT : Any, T : Host<STATE, SIDE_EFFECT>> T.assert(
    block: OrbitVerification<T, STATE, SIDE_EFFECT>.() -> Unit
) {
    val verification = OrbitVerification<T, STATE, SIDE_EFFECT>()
        .apply(block)

    @Suppress("UNCHECKED_CAST")
    val testFixtures =
        TestHarness.fixtures[this] as TestFixtures<STATE, SIDE_EFFECT>

    // sanity check the initial state
    assertEquals(
        testFixtures.initialState,
        testFixtures.stateObserver.values.firstOrNull()
    )

    assertStatesInOrder(
        testFixtures.stateObserver.values.drop(1),
        verification.expectedStateChanges,
        testFixtures.initialState
    )

    assertEquals(
        verification.expectedSideEffects,
        testFixtures.sideEffectObserver.values
    )

    verification.expectedLoopBacks.forEach {
        val f = it.invocation
        verify(
            this,
            times(it.times)
        ).f()
    }
}

class TestFixtures<STATE : Any, SIDE_EFFECT : Any>(
    val initialState: STATE,
    val stateObserver: TestStreamObserver<STATE>,
    val sideEffectObserver: TestStreamObserver<SIDE_EFFECT>
)

object TestHarness {
    val fixtures: MutableMap<Host<*, *>, TestFixtures<*, *>> = WeakHashMap()
}

fun <T : Any> Stream<T>.test() = TestStreamObserver(this)
