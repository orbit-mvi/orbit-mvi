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

interface Container<STATE : Any, SIDE_EFFECT : Any> {
    val currentState: STATE
    val stateStream: Stream<STATE>
    val sideEffectStream: Stream<SIDE_EFFECT>

    fun orbit(
        init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>
    )

    companion object {
        fun <STATE : Any, SIDE_EFFECT : Any> create(
            initialState: STATE,
            settings: Settings = Settings(),
            onCreate: (() -> Unit)? = null
        ): Container<STATE, SIDE_EFFECT> =
            if (onCreate == null) {
                RealContainer(initialState, settings)
            } else {
                LazyCreateContainerDecorator(
                    RealContainer(initialState, settings),
                    onCreate
                )
            }
    }

    class Settings(
        val sideEffectCaching: Boolean = true
    )
}
