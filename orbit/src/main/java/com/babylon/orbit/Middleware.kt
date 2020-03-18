/*
 * Copyright 2019 Babylon Partners Limited
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

package com.babylon.orbit

interface Middleware<STATE : Any, SIDE_EFFECT : Any> {
    val initialState: STATE
    val orbits: Map<String, TransformerFunction<STATE, SIDE_EFFECT>>
    val configuration: Config

    data class Config(
        val sideEffectCachingEnabled: Boolean = true,
        val testMode: Boolean = false
    )

    fun test(): Middleware<STATE, SIDE_EFFECT> {
        return object : Middleware<STATE, SIDE_EFFECT> {
            override val initialState: STATE
                get() = this@Middleware.initialState
            override val orbits: Map<String, TransformerFunction<STATE, SIDE_EFFECT>>
                get() = this@Middleware.orbits
            override val configuration: Config
                get() = this@Middleware.configuration.copy(testMode = true)
        }
    }

    fun test(flow: String): Middleware<STATE, SIDE_EFFECT> {
        return object : Middleware<STATE, SIDE_EFFECT> {
            override val initialState: STATE
                get() = this@Middleware.initialState
            override val orbits: Map<String, TransformerFunction<STATE, SIDE_EFFECT>>
                get() = this@Middleware.orbits.filter { it.key == flow }
            override val configuration: Config
                get() = this@Middleware.configuration.copy(testMode = true)
        }
    }
}
