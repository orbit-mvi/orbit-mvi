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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.babylon.orbit.internal.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

open class OrbitViewModel<STATE : Any, SIDE_EFFECT : Any>(
    private val state: SavedStateHandle? = null,
    private val container: OrbitContainer<STATE, SIDE_EFFECT>
) : ViewModel() {

    constructor(
        state: SavedStateHandle? = null,
        initialState: STATE,
        init: OrbitsBuilder<STATE, SIDE_EFFECT>.() -> Unit
    ) : this(
        state,
        BaseOrbitContainer(
            middleware(initialState, init),
            state?.get<STATE>("state") ?: initialState
        )
    )

    constructor(
        state: SavedStateHandle? = null,
        middleware: Middleware<STATE, SIDE_EFFECT>
    ) : this(
        state,
        BaseOrbitContainer(middleware, state?.get<STATE>("state") ?: middleware.initialState)
    )

    val currentState: STATE
        get() = container.currentState

    fun sendAction(action: Any) = container.sendAction(action)

    private val mainThreadOrbit =
        container.orbit.observeOn(AndroidSchedulers.mainThread())
            .doOnNext { state?.set("state", it) }

    private val mainThreadSideEffect =
        container.sideEffect.observeOn(AndroidSchedulers.mainThread())

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
        val disposables = CompositeDisposable()
        disposables += mainThreadOrbit.subscribe(stateConsumer)
        disposables += mainThreadSideEffect.subscribe(sideEffectConsumer)
        disposables.bindToLifecycle(lifecycleOwner)
    }

    override fun onCleared() {
        container.disposeOrbit()
    }
}
