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

/**
 * An Android ViewModel that hosts an Orbit MVI system.
 *
 * [connect] your ViewModel in order to use it in your Activity or Fragment.
 *
 * ### Saving state
 * You can pass an optional [SavedStateHandle] to automatically save your state using
 *
 * https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
 *
 * In order to make this work you need to be able to put your state into a bundle.
 */
open class OrbitViewModel<STATE : Any, SIDE_EFFECT : Any>(
    private val container: OrbitContainer<STATE, SIDE_EFFECT>,
    private val savedStateHandle: SavedStateHandle? = null
) : ViewModel() {

    constructor(
        initialState: STATE,
        savedStateHandle: SavedStateHandle,
        init: OrbitsBuilder<STATE, SIDE_EFFECT>.() -> Unit
    ) : this(
        BaseOrbitContainer(middleware(initialState, init), savedStateHandle.get<STATE>(STATE)),
        savedStateHandle
    )

    constructor(
        initialState: STATE,
        init: OrbitsBuilder<STATE, SIDE_EFFECT>.() -> Unit
    ) : this(BaseOrbitContainer(middleware(initialState, init)))

    constructor(
        middleware: Middleware<STATE, SIDE_EFFECT>,
        savedStateHandle: SavedStateHandle? = null
    ) : this(
        BaseOrbitContainer(middleware, savedStateHandle?.get<STATE>(STATE)),
        savedStateHandle
    )

    /**
     * Reads the current state from the Orbit container.
     */
    val currentState: STATE
        get() = container.currentState

    /**
     * Sends an action to the Orbit container.
     */
    fun sendAction(action: Any) = container.sendAction(action)

    private val mainThreadOrbit =
        container.orbit.observeOn(AndroidSchedulers.mainThread())
            .doOnNext { savedStateHandle?.set("state", it) }

    private val mainThreadSideEffect =
        container.sideEffect.observeOn(AndroidSchedulers.mainThread())

    /**
     * Designed to be called in onCreate, onStart or onResume, depending on your use case.
     * The subscription will be disposed in the method symmetric to the one it was called in.
     * For example onCreate -> onDestro, onStart -> onStop, onResume -> onPause.
     *
     * @param lifecycleOwner The LifecycleOwner whose lifecycle callbacks are listened to in order to schedule unsubscription.
     * @param stateConsumer A function that will get called every time the state updates.
     */
    fun connect(
        lifecycleOwner: LifecycleOwner,
        stateConsumer: (STATE) -> Unit
    ) {
        connect(lifecycleOwner, stateConsumer, {})
    }

    /**
     * Designed to be called in onCreate, onStart or onResume, depending on your use case.
     * The subscription will be disposed in the method symmetric to the one it was called in.
     * For example  onCreate -> onDestroy, onStart -> onStop, onResume -> onPause.
     *
     * @param lifecycleOwner The LifecycleOwner whose lifecycle callbacks are listened to in order to schedule unsubscription.
     * @param stateConsumer A function that will get called every time the state updates.
     * @param sideEffectConsumer A function that will get called every time side effects are posted from the Orbit container.
     */
    fun connect(
        lifecycleOwner: LifecycleOwner,
        stateConsumer: (STATE) -> Unit,
        sideEffectConsumer: (SIDE_EFFECT) -> Unit
    ) {
        CompositeDisposable().apply {
            this += mainThreadOrbit.subscribe(stateConsumer)
            this += mainThreadSideEffect.subscribe(sideEffectConsumer)
            bindToLifecycle(lifecycleOwner)
        }
    }

    override fun onCleared() {
        container.disposeOrbit()
    }

    private companion object Key {
        const val STATE = "state"
    }
}
