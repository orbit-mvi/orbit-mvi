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

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observables.ConnectableObservable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject

class BaseOrbitContainer<STATE : Any, SIDE_EFFECT : Any>(
    middleware: Middleware<STATE, SIDE_EFFECT>
) : OrbitContainer<STATE, SIDE_EFFECT> {

    var state: Single<STATE>
        private set

    private val inputRelay: PublishSubject<Any> = PublishSubject.create()
    override val orbit: ConnectableObservable<STATE>
    override val sideEffect: Observable<SIDE_EFFECT> = middleware.sideEffect

    private val disposables = CompositeDisposable()

    init {
        state = Single.just(middleware.initialState)
        orbit = inputRelay.doOnSubscribe { disposables += it }
            .startWith(LifecycleAction.Created)
            .map { ActionState(state.blockingGet(), it) } // Attaches the current state to the event
            .buildOrbit(middleware, inputRelay)
            .replay(1)
        orbit.connect { disposables += it }
        state = orbit
            .first(middleware.initialState)
    }

    override fun sendAction(action: Any) {
        inputRelay.onNext(action)
    }

    override fun disposeOrbit() {
        disposables.clear()
    }
}
