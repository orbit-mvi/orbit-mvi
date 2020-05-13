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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import kotlin.test.assertEquals

/**
 * This class applies the given [OrbitVerification] assertions after an [invocation] on
 * the host starting with a given initial state as defined in [OrbitGiven].
 *
 * The assertions that are run include:
 * 1. Sequence of states
 * 2. Sequence of posted side effects
 * 3. Loopbacks i.e. invocations of other flows
 * 5. No other interactions - makes sure there are no unexpected side effects of a flow
 */
class OrbitInvocation<HOST : Host<STATE, SIDE_EFFECT>, STATE : Any, SIDE_EFFECT : Any>(
    private val host: HOST,
    private val initialState: STATE,
    private val invocation: HOST.() -> Unit
) {
    fun then(block: OrbitVerification<HOST, STATE, SIDE_EFFECT>.() -> Unit) {

        val orbitTestObserver = host.container.orbit.test()
        val sideEffectTestObserver = host.container.sideEffect.test()
        host.invocation()

        val verification = OrbitVerification<HOST, STATE, SIDE_EFFECT>()
            .apply(block)

        // sanity check the initial state
        assertEquals(
            initialState,
            orbitTestObserver.values.firstOrNull()
        )

        assertStatesInOrder(
            orbitTestObserver.values.drop(1),
            verification.expectedStateChanges,
            initialState
        )

        assertEquals(
            verification.expectedSideEffects,
            sideEffectTestObserver.values
        )

        verify(
            host,
            atLeast(0)
        ).orbit<Any>(
            any(),
            any()
        )
        verify(
            host,
            atLeast(0)
        ).container
        verify(
            host,
            atLeast(0)
        ).invocation()

        verification.expectedLoopBacks.forEach {
            val f = it.invocation
            verify(
                host,
                times(it.times)
            ).f()
        }

        verifyNoMoreInteractions(host)
    }
}
