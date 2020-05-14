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

import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class LazyCreateContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    private val actual: Container<STATE, SIDE_EFFECT>,
    private val onCreate: () -> Unit
) : Container<STATE, SIDE_EFFECT> {
    private val created = AtomicBoolean(false)

    override val currentState: STATE
        get() = actual.currentState

    override val orbit: Stream<STATE>
        get() = object : Stream<STATE> {
            override fun observe(lambda: (STATE) -> Unit): Closeable {
                runOnCreate()
                return actual.orbit.observe(lambda)
            }
        }
    override val sideEffect: Stream<SIDE_EFFECT>
        get() = object : Stream<SIDE_EFFECT> {
            override fun observe(lambda: (SIDE_EFFECT) -> Unit): Closeable {
                runOnCreate()
                return actual.sideEffect.observe(lambda)
            }
        }

    override fun <EVENT : Any> orbit(
        event: EVENT,
        init: Builder<STATE, SIDE_EFFECT, EVENT>.() -> Builder<STATE, SIDE_EFFECT, *>
    ) = runOnCreate().also { actual.orbit(event, init) }

    private fun runOnCreate() {
        if (created.compareAndSet(false, true)) {
            onCreate()
        }
    }
}
