/*
 * Copyright 2021-2022 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.StateFlow

/**
 * Observe [StateFlow] of [SideEffectFlow] in a Compose [LaunchedEffect].
 * @param lifecycleState [Lifecycle.State] in which [state] block runs.
 */
@SuppressLint("ComposableNaming")
@Composable
fun <SIDE_EFFECT : Any> StateFlow<SIDE_EFFECT>.collectSideEffectLifecycleAware(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    sideEffect: (suspend (sideEffect: SIDE_EFFECT) -> Unit)
) {
    val sideEffectFlow = this
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(sideEffectFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            sideEffectFlow.collect { sideEffect(it) }
        }
    }
}

/**
 * Observe [StateFlow] of [STATE] in a Compose [LaunchedEffect].
 * @param lifecycleState [Lifecycle.State] in which [state] block runs.
 */
@SuppressLint("ComposableNaming")
@Composable
fun <STATE : Any> StateFlow<STATE>.collectStateLifecycleAware(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    state: (suspend (state: STATE) -> Unit)
) {
    val stateFlow = this
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(stateFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            stateFlow.collect { state(it) }
        }
    }
}

/**
 * Observe [StateFlow] of [STATE] as [State].
 * @param lifecycleState The Lifecycle where the restarting collecting from this flow work will be kept alive.
 */
@Composable
fun <STATE : Any> StateFlow<STATE>.collectAsStateLifecycleAware(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED
): State<STATE> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val stateFlowLifecycleAware = remember(this, lifecycleOwner) {
        this.flowWithLifecycle(lifecycleOwner.lifecycle, lifecycleState)
    }
    // Need to access the initial value to convert to State - collectAsState() suppresses this lint warning too
    @SuppressLint("StateFlowValueCalledInComposition")
    val initialValue = this.value
    return stateFlowLifecycleAware.collectAsState(initialValue)
}
