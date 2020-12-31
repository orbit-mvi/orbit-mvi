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

package org.orbitmvi.orbit.internal

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

public class LazyCreateContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    override val actual: Container<STATE, SIDE_EFFECT>,
    public val onCreate: (state: STATE) -> Unit
) : ContainerDecorator<STATE, SIDE_EFFECT> {
    private val created = atomic<Int>(0)

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

    private fun runOnCreate() {
        if (created.compareAndSet(0, 1)) {
            onCreate(actual.currentState)
        }
    }

    override fun orbit(orbitFlow: suspend OrbitDslPlugin.ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        runOnCreate().also { actual.orbit(orbitFlow) }
    }
}
