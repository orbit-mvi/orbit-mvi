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

package org.orbitmvi.orbit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHostWithExternalState
import org.orbitmvi.orbit.syntax.Syntax

/**
 * Observe [Container.sideEffectFlow] in a Compose [LaunchedEffect].
 *
 * Active subscriptions from this operator count towards [Syntax.repeatOnSubscription] subscribers.
 *
 * @param lifecycleState [Lifecycle.State] in which side effects are collected.
 */
@Composable
public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>.collectSideEffect(
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
 * Observe [Container.stateFlow] as [State].
 * @param lifecycleState The minimum lifecycle state at which the state is observed.
 *
 * Active subscriptions from this operator count towards [Syntax.repeatOnSubscription] subscribers.
 */
@Composable
public fun <INTERNAL_STATE : Any, EXTERNAL_STATE : Any, SIDE_EFFECT : Any> ContainerHostWithExternalState<INTERNAL_STATE, EXTERNAL_STATE, SIDE_EFFECT>.collectAsState(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED
): State<EXTERNAL_STATE> {
    return container.externalRefCountStateFlow.collectAsStateWithLifecycle(minActiveState = lifecycleState)
}
