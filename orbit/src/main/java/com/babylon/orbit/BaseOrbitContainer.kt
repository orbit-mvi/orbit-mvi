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

import hu.akarnokd.rxjava2.subjects.UnicastWorkSubject
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

    private val inputSubject: PublishSubject<Any> = PublishSubject.create()
    private val reducerSubject: PublishSubject<(STATE) -> STATE> = PublishSubject.create()
    private val sideEffectSubject: PublishSubject<SIDE_EFFECT> = PublishSubject.create()
    private val disposables = CompositeDisposable()

    @Volatile
    override var currentState: STATE = middleware.initialState
        private set
    override val orbit: ConnectableObservable<STATE>
    override val sideEffect: Observable<SIDE_EFFECT> =
        if (middleware.configuration.sideEffectCachingEnabled) {
            UnicastWorkSubject.create<SIDE_EFFECT>()
                .also { sideEffectSubject.subscribe(it) }
                .publish()
                .refCount()
        } else {
            sideEffectSubject
        }

    init {
        val scheduler = createSingleScheduler()

        disposables += inputSubject.doOnSubscribe { disposables += it }
            .startWith(LifecycleAction.Created)
            .observeOn(scheduler)
            .publish { actions ->
                with(
                    OrbitContext(
                        { currentState },
                        actions,
                        inputSubject,
                        reducerSubject,
                        sideEffectSubject,
                        false
                    )
                ) {
                    Observable.merge(
                        middleware.orbits.map { transformer ->
                            transformer()
                        }
                    )
                }
            }
            .subscribe()

        orbit = reducerSubject
            .observeOn(scheduler)
            .scan(middleware.initialState) { currentState, partialReducer ->
                partialReducer(
                    currentState
                )
            }
            .doOnNext { currentState = it }
            .distinctUntilChanged()
            .replay(1)

        orbit.connect { disposables += it }
    }

    override fun sendAction(action: Any) {
        inputSubject.onNext(action)
    }

    override fun disposeOrbit() {
        disposables.clear()
    }

    private fun createSingleScheduler(): Scheduler {
        return Schedulers.from(Executors.newSingleThreadExecutor { Thread(it, "reducerThread") })
    }
}
