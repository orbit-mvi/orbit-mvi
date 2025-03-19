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

package org.orbitmvi.orbit.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost

/**
 * Observe [Container.stateFlow] and [Container.sideEffectFlow] correctly on Android in one-line of code.
 * These streams are observed when the view is in [Lifecycle.State.STARTED].
 *
 * In Activities, call from onCreate, where viewModel is a [ContainerHost]:
 *
 * ```
 * viewModel.observe(this, state = ::state, sideEffect = ::sideEffect)
 * ```
 *
 * In Fragments, call from onViewCreated, where viewModel is a [ContainerHost]:
 *
 * ```
 * viewModel.observe(viewLifecycleOwner, state = ::state, sideEffect = ::sideEffect)
 * ```
 */
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.observe(
    lifecycleOwner: LifecycleOwner,
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    state: (suspend (state: STATE) -> Unit)? = null,
    sideEffect: (suspend (sideEffect: SIDE_EFFECT) -> Unit)? = null
) {
    lifecycleOwner.lifecycleScope.launch {
        // See https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
        lifecycleOwner.lifecycle.repeatOnLifecycle(lifecycleState) {
            state?.let { launch { container.refCountStateFlow.collect { state(it) } } }
            sideEffect?.let { launch { container.refCountSideEffectFlow.collect { sideEffect(it) } } }
        }
    }
}
