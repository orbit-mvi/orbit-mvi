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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost

/**
 * Observe [Container.sideEffectFlow] in a Compose [LaunchedEffect].
 */
@SuppressLint("ComposableNaming")
@Composable
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.collectSideEffect(
    sideEffect: (suspend (sideEffect: SIDE_EFFECT) -> Unit)
) {
    val sideEffectFlow = container.sideEffectFlow
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(sideEffectFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            sideEffectFlow.collect { sideEffect(it) }
        }
    }
}

/**
 * Observe [Container.stateFlow] in a Compose [LaunchedEffect].
 */
@SuppressLint("ComposableNaming")
@Composable
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.collectState(
    state: (suspend (state: STATE) -> Unit)
) {
    val stateFlow = container.stateFlow
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(stateFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            stateFlow.collect { state(it) }
        }
    }
}

/**
 * Observe [Container.stateFlow] as [State].
 */
@Composable
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.collectAsState(): State<STATE> {
    val stateFlow = container.stateFlow
    val lifecycleOwner = LocalLifecycleOwner.current

    val stateFlowLifecycleAware = remember(stateFlow, lifecycleOwner) {
        stateFlow.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
    }

    // Need to access the initial value to convert to State - collectAsState() suppresses this lint warning too
    @SuppressLint("StateFlowValueCalledInComposition")
    val initialValue = stateFlow.value
    return stateFlowLifecycleAware.collectAsState(initialValue)
}
