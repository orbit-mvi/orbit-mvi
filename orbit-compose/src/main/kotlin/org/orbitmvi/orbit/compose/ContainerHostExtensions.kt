/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.ContainerHostWithExtState
import org.orbitmvi.orbit.ContainerWithExtState
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription

/**
 * Observe [Container.sideEffectFlow] in a Compose [LaunchedEffect].
 *
 * Active subscriptions from this operator count towards [repeatOnSubscription] subscribers.
 *
 * @param lifecycleState [Lifecycle.State] in which [state] block runs.
 */
@SuppressLint("ComposableNaming")
@Composable
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.collectSideEffect(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    sideEffect: (suspend (sideEffect: SIDE_EFFECT) -> Unit)
) {
    val sideEffectFlow = container.refCountSideEffectFlow
    val lifecycleOwner = LocalLifecycleOwner.current

    val callback by rememberUpdatedState(newValue = sideEffect)

    LaunchedEffect(sideEffectFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            sideEffectFlow.collect { callback(it) }
        }
    }
}

/**
 * Observe [Container.stateFlow] in a Compose [LaunchedEffect].
 * @param lifecycleState [Lifecycle.State] in which [state] block runs.
 *
 * Active subscriptions from this operator count towards [repeatOnSubscription] subscribers.
 */
@SuppressLint("ComposableNaming")
@Composable
@Deprecated("This API will no longer be supported for Compose. Use collectAsState or container.refCountStateFlow instead")
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.collectState(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    state: (suspend (state: STATE) -> Unit)
) {
    val stateFlow = container.refCountStateFlow
    val lifecycleOwner = LocalLifecycleOwner.current

    val callback by rememberUpdatedState(newValue = state)

    LaunchedEffect(stateFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            stateFlow.collect { callback(it) }
        }
    }
}

/**
 * Observe [Container.stateFlow] as [State].
 * @param lifecycleState The minimum lifecycle state at which the state is observed.
 *
 * Active subscriptions from this operator count towards [repeatOnSubscription] subscribers.
 */
@Composable
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.collectAsState(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED
): State<STATE> {
    return container.refCountStateFlow.collectAsStateWithLifecycle(minActiveState = lifecycleState)
}

/**
 * Observe the [ContainerWithExtState.extStateFlow] as [State].
 *
 * @param lifecycleState The minimum lifecycle state at which the state is observed.
 *
 * Active subscriptions from this operator count towards [repeatOnSubscription] subscribers.
 */
@Composable
public fun <STATE : Any, SIDE_EFFECT : Any, EXT_STATE : Any> ContainerHostWithExtState<
    STATE,
    SIDE_EFFECT,
    EXT_STATE,
    >.collectExtState(lifecycleState: Lifecycle.State = androidx.lifecycle.Lifecycle.State.STARTED): State<EXT_STATE> {
    return container.extStateFlow.collectAsStateWithLifecycle(minActiveState = lifecycleState)
}
