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

import androidx.lifecycle.ViewModel
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDispose
import io.reactivex.Observable

abstract class OrbitViewModel<STATE : Any, EVENT : Any>(
    middleware: Middleware<STATE, EVENT>
) : ViewModel() {

    private val container: AndroidOrbitContainer<STATE, EVENT> = AndroidOrbitContainer(middleware)

    val state: STATE
        get() = container.state

    /**
     * Designed to be called in onStart or onResume, depending on your use case.
     * DO NOT call in other lifecycle methods unless you know what you're doing!
     * The subscriptions will be disposed in methods symmetric to the ones they were called in.
     * For example onStart -> onStop, onResume -> onPause, onCreate -> onDestroy.
     */
    fun connect(
        scoper: AndroidLifecycleScopeProvider,
        actions: Observable<out Any>,
        stateConsumer: (STATE) -> Unit,
        eventConsumer: (EVENT) -> Unit = {}
    ) {

        container.orbit
            .autoDispose(scoper)
            .subscribe(stateConsumer)

        actions.autoDispose(scoper)
            .subscribe(container.inputRelay::accept)

        container.sideEffect
            .autoDispose(scoper)
            .subscribe(eventConsumer)
    }

    override fun onCleared() {
        container.disposeOrbit()
    }
}
