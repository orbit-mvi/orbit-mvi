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

class OrbitVerification<HOST : Host<STATE, SIDE_EFFECT>, STATE : Any, SIDE_EFFECT : Any> {
    internal var expectedSideEffects = emptyList<SIDE_EFFECT>()
    internal var expectedStateChanges = emptyList<STATE.() -> STATE>()
    internal var expectedLoopBacks = mutableListOf<Times<HOST, STATE, SIDE_EFFECT>>()

    fun states(vararg expectedStateChanges: STATE.() -> STATE) {
        this.expectedStateChanges = expectedStateChanges.toList()
    }

    fun postedSideEffects(vararg expectedSideEffects: SIDE_EFFECT) {
        this.expectedSideEffects = expectedSideEffects.toList()
    }

    fun postedSideEffects(expectedSideEffects: Iterable<SIDE_EFFECT>) {
        this.expectedSideEffects = expectedSideEffects.toList()
    }

    fun loopBack(times: Int = 1, block: HOST.() -> Unit) {
        this.expectedLoopBacks.add(
            Times(
                times,
                block
            )
        )
    }

    data class Times<HOST : Host<STATE, SIDE_EFFECT>, STATE : Any, SIDE_EFFECT : Any>(
        val times: Int = 1,
        val invocation: HOST.() -> Unit
    )
}
