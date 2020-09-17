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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean

class LazyCreateContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    override val actual: Container<STATE, SIDE_EFFECT>,
    val onCreate: (state: STATE) -> Unit
) : ContainerDecorator<STATE, SIDE_EFFECT> {
    private val created = AtomicBoolean(false)

    override val currentState: STATE
        get() = actual.currentState

    override val stateStream: Flow<STATE>
        get() = flow {
            runOnCreate()
            emitAll(actual.stateStream)
        }

    override val sideEffectStream: Flow<SIDE_EFFECT>
        get() = flow {
            runOnCreate()
            emitAll(actual.sideEffectStream)
        }

    override fun orbit(
        init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>
    ) = runOnCreate().also { actual.orbit(init) }

    private fun runOnCreate() {
        if (created.compareAndSet(false, true)) {
            onCreate(actual.currentState)
        }
    }
}
