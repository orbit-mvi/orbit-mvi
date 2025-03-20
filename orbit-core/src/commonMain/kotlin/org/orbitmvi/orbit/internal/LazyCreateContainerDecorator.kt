/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.internal

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.syntax.ContainerContext
import org.orbitmvi.orbit.syntax.intent
import kotlin.concurrent.atomics.AtomicBoolean

public class LazyCreateContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    override val actual: Container<STATE, SIDE_EFFECT>,
    public val onCreate: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit
) : ContainerDecorator<STATE, SIDE_EFFECT> {
    private val created = AtomicBoolean(false)

    override val stateFlow: StateFlow<STATE> = actual.stateFlow.onSubscribe { runOnCreate() }
    override val refCountStateFlow: StateFlow<STATE> = actual.refCountStateFlow.onSubscribe { runOnCreate() }
    override val sideEffectFlow: Flow<SIDE_EFFECT> = flow {
        runOnCreate()
        emitAll(actual.sideEffectFlow)
    }
    override val refCountSideEffectFlow: Flow<SIDE_EFFECT> = flow {
        runOnCreate()
        emitAll(actual.refCountSideEffectFlow)
    }

    private fun runOnCreate() {
        if (created.compareAndSet(expectedValue = false, newValue = true)) {
            actual.intent(registerIdling = false, transformer = onCreate)
        }
    }

    override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit): Job {
        runOnCreate().also { return actual.orbit(orbitIntent) }
    }

    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        runOnCreate().also { actual.inlineOrbit(orbitIntent) }
    }
}
