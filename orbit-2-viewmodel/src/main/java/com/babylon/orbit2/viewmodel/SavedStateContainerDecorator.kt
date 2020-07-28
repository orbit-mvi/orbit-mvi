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

package com.babylon.orbit2.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.babylon.orbit2.Builder
import com.babylon.orbit2.Container
import com.babylon.orbit2.Stream
import java.io.Closeable

internal class SavedStateContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    private val actual: Container<STATE, SIDE_EFFECT>,
    private val savedStateHandle: SavedStateHandle
) : Container<STATE, SIDE_EFFECT> {
    override val currentState: STATE
        get() = actual.currentState

    override val stateStream: Stream<STATE>
        get() = object : Stream<STATE> {
            override fun observe(lambda: (STATE) -> Unit): Closeable {
                return actual.stateStream.observe {
                    savedStateHandle[SAVED_STATE_KEY] = it
                    lambda(it)
                }
            }
        }

    override val sideEffectStream: Stream<SIDE_EFFECT>
        get() = actual.sideEffectStream

    override fun orbit(
        init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>
    ) = actual.orbit(init)
}
