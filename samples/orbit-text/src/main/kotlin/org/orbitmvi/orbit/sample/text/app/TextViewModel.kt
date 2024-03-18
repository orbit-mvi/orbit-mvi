/*
 * Copyright 2022 Mikołaj Leszczyński & Appmattus Limited
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
 */

package org.orbitmvi.orbit.sample.text.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.random.Random

@Suppress("MagicNumber")
class TextViewModel : ContainerHost<TextViewModel.State, Nothing> {
    private val scope = CoroutineScope(Dispatchers.Main)
    override val container: Container<State, Nothing> = scope.container(State())

    fun updateTextBad(text: String) = intent {
        // simulate considerable load on the device
        delay(Random.nextLong(30, 60))
        reduce { state.copy(badField = text) }
    }

    fun updateTextGood(text: String) = blockingIntent {
        // simulate considerable load on the device
        delay(Random.nextLong(30, 60))
        reduce { state.copy(goodField = text) }
    }

    data class State(
        val badField: String = "",
        val goodField: String = "",
    )
}
