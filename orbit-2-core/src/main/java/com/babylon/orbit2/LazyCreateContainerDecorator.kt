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
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class LazyCreateContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    override val actual: Container<STATE, SIDE_EFFECT>,
    val onCreate: (state: STATE) -> Unit
) : ContainerDecorator<STATE, SIDE_EFFECT> {
    private val created = AtomicBoolean(false)

    override val currentState: STATE
        get() = actual.currentState

    override val stateFlow: Flow<STATE>
        get() = flow {
            runOnCreate()
            emitAll(actual.stateFlow)
        }

    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = flow {
            runOnCreate()
            emitAll(actual.sideEffectFlow)
        }

    override val stateStream: Stream<STATE>
        get() = object : Stream<STATE> {
            override fun observe(lambda: (STATE) -> Unit): Closeable {
                runOnCreate()
                @Suppress("DEPRECATION")
                return actual.stateStream.observe(lambda)
            }
        }

    override val sideEffectStream: Stream<SIDE_EFFECT>
        get() = object : Stream<SIDE_EFFECT> {
            override fun observe(lambda: (SIDE_EFFECT) -> Unit): Closeable {
                runOnCreate()
                @Suppress("DEPRECATION")
                return actual.sideEffectStream.observe(lambda)
            }
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
