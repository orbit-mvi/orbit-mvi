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
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observables.ConnectableObservable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Executors

class BaseOrbitContainer<STATE : Any, SIDE_EFFECT : Any>(
    middleware: Middleware<STATE, SIDE_EFFECT>
) : OrbitContainer<STATE, SIDE_EFFECT> {

    private val inputRelay: PublishSubject<Any> = PublishSubject.create()
    private val disposables = CompositeDisposable()

    @Volatile
    override var currentState: STATE = middleware.initialState
        private set
    override val orbit: ConnectableObservable<STATE>
    override val sideEffect: Observable<SIDE_EFFECT> = middleware.sideEffect

    init {
        orbit = inputRelay.doOnSubscribe { disposables += it }
            .startWith(LifecycleAction.Created)
            .buildOrbit(middleware)
            .doOnNext { currentState = it }
            .replay(1)

        orbit.connect { disposables += it }
    }

    override fun sendAction(action: Any) {
        inputRelay.onNext(action)
    }

    override fun disposeOrbit() {
        disposables.clear()
    }

    private fun Observable<*>.buildOrbit(middleware: Middleware<STATE, SIDE_EFFECT>): Observable<STATE> {
        val scheduler = createSingleScheduler()
        return this
            .observeOn(scheduler)
            .publish { actions ->
                with(
                    OrbitContext(
                        { currentState },
                        actions,
                        inputRelay,
                        false
                    )
                ) {
                    Observable.merge(
                        middleware.orbits.map { transformer ->
                            transformer()
                        }
                    )
                }
                    .observeOn(scheduler)
                    .scan(middleware.initialState) { currentState, partialReducer ->
                        partialReducer(
                            currentState
                        )
                    }
                    .distinctUntilChanged()
            }
    }

    private fun createSingleScheduler(): Scheduler {
        return Schedulers.from(Executors.newSingleThreadExecutor())
    }
}
