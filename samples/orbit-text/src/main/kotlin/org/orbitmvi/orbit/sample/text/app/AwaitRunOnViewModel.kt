/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.orbitContainer

/**
 * Demonstrates [awaitRunOn] with a sealed class state.
 *
 * The container starts in [UiState.Loading] and transitions to [UiState.Ready] after a simulated
 * delay. The [awaitRunOn] call in onCreate suspends until the state becomes [UiState.Ready], then
 * begins observing the TextField for validation — without manual flags or stateFlow filtering.
 */
@Suppress("MagicNumber")
class AwaitRunOnViewModel : OrbitContainerHost<AwaitRunOnViewModel.UiState, AwaitRunOnViewModel.UiState, Nothing> {
    private val scope = CoroutineScope(Dispatchers.Main)

    @OptIn(OrbitExperimental::class)
    override val container: OrbitContainer<UiState, UiState, Nothing> = scope.orbitContainer(UiState.Loading) {
        coroutineScope {
            // Simulate loading data before the form is ready
            launch {
                delay(2000)
                reduce { UiState.Ready() }
            }

            // awaitRunOn suspends here until state becomes Ready, then starts validation.
            // No manual flags or stateFlow filtering needed.
            launch {
                awaitRunOn<UiState.Ready> {
                    snapshotFlow { state.textFieldState.text }.collectLatest { text ->
                        reduce { state.copy(validationError = text.validate()) }
                    }
                }
            }
        }
    }

    sealed interface UiState {
        data object Loading : UiState

        data class Ready(
            val textFieldState: TextFieldState = TextFieldState(""),
            val validationError: String? = null,
        ) : UiState
    }

    companion object {
        private fun CharSequence.validate(): String? = when {
            isBlank() -> "Must not be blank"
            length > 10 -> "Must be 10 characters or fewer"
            else -> null
        }
    }
}
