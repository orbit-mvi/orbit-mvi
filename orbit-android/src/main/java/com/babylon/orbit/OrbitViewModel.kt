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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

open class OrbitViewModel<STATE : Any, SIDE_EFFECT : Any>(
    private val container: OrbitContainer<STATE, SIDE_EFFECT>
) : ViewModel() {

    private val disposables = CompositeDisposable()

    constructor(
        initialState: STATE,
        init: OrbitsBuilder<STATE, SIDE_EFFECT>.() -> Unit
    ) : this(BaseOrbitContainer(middleware(initialState, init)))

    constructor(middleware: Middleware<STATE, SIDE_EFFECT>) : this(BaseOrbitContainer(middleware))

    val currentState: STATE
        get() = container.currentState

    fun sendAction(action: Any) = container.sendAction(action)

    private val mainThreadOrbit = container.orbit.observeOn(AndroidSchedulers.mainThread())
    private val mainThreadSideEffect = container.sideEffect.observeOn(AndroidSchedulers.mainThread())

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
        disposables += mainThreadOrbit.subscribe(stateConsumer)
        disposables += mainThreadSideEffect.subscribe(sideEffectConsumer)

        val exitEvent = lifecycleOwner.lifecycle.currentState.correspondingExitEvent
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == exitEvent) {
                    disposables.clear()
                    source.lifecycle.removeObserver(this)
                }
            }
        })
    }

    override fun onCleared() {
        disposables.dispose()
        container.disposeOrbit()
    }

    private val Lifecycle.State.correspondingExitEvent: Lifecycle.Event
        get() = when (this) {
            Lifecycle.State.DESTROYED -> error("Lifecycle is already destroyed")
            // in onCreate
            Lifecycle.State.INITIALIZED -> Lifecycle.Event.ON_DESTROY
            // in onStart
            Lifecycle.State.CREATED -> Lifecycle.Event.ON_STOP
            // in onResume
            Lifecycle.State.STARTED -> Lifecycle.Event.ON_PAUSE
            // between onResume and onPause
            Lifecycle.State.RESUMED -> Lifecycle.Event.ON_PAUSE
        }
}
