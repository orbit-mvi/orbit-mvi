/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost

/**
 * Observe [Container.stateFlow] and [Container.sideEffectFlow] correctly on Android in one-line of code.
 * By default these streams will be observed at [Lifecycle.State.STARTED].
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
 *
 * See https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
 */
fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.observe(
    lifecycleOwner: LifecycleOwner,
    stateActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    state: (suspend (state: STATE) -> Unit)? = null,
    sideEffectActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    sideEffect: (suspend (sideEffect: SIDE_EFFECT) -> Unit)? = null
) {
    with(lifecycleOwner) {
        state?.let {
            lifecycleScope.launch {
                // repeatOnLifecycle launches the block in a new coroutine every time the
                // lifecycle is in the `stateActiveState` state (or above) and cancels it when it's not.
                repeatOnLifecycle(stateActiveState) {
                    container.stateFlow.collect(state)
                }
            }
        }

        sideEffect?.let {
            lifecycleScope.launch {
                container.sideEffectFlow.flowWithLifecycle(lifecycle, sideEffectActiveState).collect(sideEffect)
            }
        }
    }
}
