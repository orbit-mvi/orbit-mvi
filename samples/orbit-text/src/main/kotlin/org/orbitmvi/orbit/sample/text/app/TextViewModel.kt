/*
 * Copyright 2022-2025 Mikołaj Leszczyński & Appmattus Limited
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

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.container
import kotlin.random.Random

@Suppress("MagicNumber")
class TextViewModel : ContainerHost<TextViewModel.State, Nothing> {
    private val scope = CoroutineScope(Dispatchers.Main)
    override val container: Container<State, Nothing> = scope.container(State()) {
        coroutineScope {
            launch {
                snapshotFlow { state.textFieldState.text }.collectLatest { text ->
                    reduce { state.copy(isTextFieldStateInError = !text.isValid()) }
                }
            }
        }
    }

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

    fun submit() = intent {
        reduce {
            val text = state.textFieldState.text
            state.copy(result = "Result: \"$text\" isValid=${text.isValid()}")
        }
    }

    data class State(
        val badField: String = "",
        val goodField: String = "",
        val textFieldState: TextFieldState = TextFieldState(""),
        val isTextFieldStateInError: Boolean = false,
        val result: String = ""
    )

    companion object {
        fun CharSequence.isValid(): Boolean {
            return this.isNotBlank() && this.length <= 10
        }
    }
}
