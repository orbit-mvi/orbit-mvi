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

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.uber.autodispose.android.lifecycle.autoDispose

open class OrbitViewModel<STATE : Any, SIDE_EFFECT : Any>(
    middleware: Middleware<STATE, SIDE_EFFECT>
) : ViewModel(), OrbitContainer<STATE, SIDE_EFFECT> by AndroidOrbitContainer(middleware) {

    constructor(
        initialState: STATE,
        init: OrbitsBuilder<STATE, SIDE_EFFECT>.() -> Unit
    ) : this(middleware(initialState, init))

    /**
     * Designed to be called in onStart or onResume, depending on your use case.
     * DO NOT call in other lifecycle methods unless you know what you're doing!
     * The subscriptions will be disposed in methods symmetric to the ones they were called in.
     * For example onStart -> onStop, onResume -> onPause, onCreate -> onDestroy.
     */
    fun connect(
        lifecycleOwner: LifecycleOwner,
        stateConsumer: (STATE) -> Unit,
        sideEffectConsumer: (SIDE_EFFECT) -> Unit = {}
    ) {

        orbit.autoDispose(lifecycleOwner)
            .subscribe(stateConsumer)

        sideEffect.autoDispose(lifecycleOwner)
            .subscribe(sideEffectConsumer)
    }

    override fun onCleared() {
        disposeOrbit()
    }
}
