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

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever

/*
Things to verify:
1. Sequence of states
2. Sequence of side effects
3. "loopbacks"
4. Invocations on dependencies (mocks)
5. No other interactions
 */

fun <STATE : Any, SIDE_EFFECT : Any, T : Host<STATE, SIDE_EFFECT>> T.testSpy(
    initialState: STATE,
    isolateFlow: Boolean
): T {
    val spy = spy(this)
    val container = TestContainer<STATE, SIDE_EFFECT>(
        initialState,
        isolateFlow
    )
    doAnswer { container }.whenever(spy).container
    return spy
}

fun <HOST : Host<STATE, SIDE_EFFECT>, STATE : Any, SIDE_EFFECT : Any>
        HOST.given(
            initialState: STATE,
            isolateFlow: Boolean = false
        ) =
    OrbitGiven(
        testSpy(initialState, isolateFlow),
        initialState
    )

fun <T : Any> Stream<T>.test() = TestStreamObserver(this)

