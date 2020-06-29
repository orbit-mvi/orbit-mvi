/*
 * Copyright 2020 Babylon Partners Limited
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
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BaseOrbitContainer<STATE : Any, SIDE_EFFECT : Any>(
    private val middleware: Middleware<STATE, SIDE_EFFECT>,
    initialStateOverride: STATE? = null
) : OrbitContainer<STATE, SIDE_EFFECT> {

    private val inputSubject: PublishSubject<Any> = PublishSubject.create()
    private val reducerSubject: BehaviorSubject<STATE> =
        BehaviorSubject.createDefault(initialStateOverride ?: middleware.initialState)
    private val sideEffectSubject: PublishSubject<SIDE_EFFECT> = PublishSubject.create()
    private val disposables = CompositeDisposable()
    private val scheduler = orbitScheduler(middleware.configuration)
    private var executor: ExecutorService? = null

    override val orbit: Observable<STATE> = reducerSubject.distinctUntilChanged()
    override val currentState: STATE
        get() = orbit.blockingFirst()
    override val sideEffect: Observable<SIDE_EFFECT> =
        if (middleware.configuration.sideEffectCachingEnabled) {
            UnicastWorkSubject.create<SIDE_EFFECT>()
                .also { sideEffectSubject.subscribe(it) }
                .publish()
                .refCount()
        } else {
            sideEffectSubject
        }

    private fun getContext(actions: Observable<Any>) = OrbitContext(
        { currentState },
        actions,
        inputSubject,
        ::reduce,
        sideEffectSubject,
        backgroundScheduler(middleware.configuration)
    )

    init {
        disposables += inputSubject.doOnSubscribe { disposables += it }
            .observeOn(scheduler)
            .publish { actions ->
                with(getContext(actions)) {
                    Observable.merge(
                        middleware.orbits.values.map { transformers ->
                            transformers.last()()
                        }
                    )
                }
            }
            .subscribeBy(
                onError = { handleThrowable(it) }
            )

        // only emit [LifecycleAction.Created] if we didn't override the initial state
        if (initialStateOverride == null) inputSubject.onNext(LifecycleAction.Created)
    }

    private fun reduce(partialReducer: (STATE) -> STATE): Single<STATE> {
        return Single.fromCallable {
            reducerSubject.onNext(partialReducer(currentState))
            currentState
        }
            .subscribeOn(scheduler)
            .onErrorResumeNext { throwable: Throwable ->
                handleThrowable(throwable)
                Single.error(throwable)
            }
    }

    private fun handleThrowable(throwable: Throwable) {
        val orbitException = OrbitException(throwable)
        orbitException.stackTrace = orbitException.stackTrace
            .takeWhile { it.className.startsWith("com.babylon.orbit") }
            .toTypedArray()
        Thread.currentThread().uncaughtExceptionHandler
            .uncaughtException(Thread.currentThread(), orbitException)
    }

    override fun sendAction(action: Any) {
        inputSubject.onNext(action)
    }

    override fun disposeOrbit() {
        disposables.clear()
        executor?.shutdown()
    }

    private fun backgroundScheduler(configuration: Middleware.Config): Scheduler {
        return if (configuration.testMode) {
            Schedulers.trampoline()
        } else {
            Schedulers.io()
        }
    }

    private fun orbitScheduler(configuration: Middleware.Config): Scheduler {
        return if (configuration.testMode) {
            Schedulers.trampoline()
        } else {
            Executors.newSingleThreadExecutor {
                Thread(it, "reducerThread")
            }.let {
                executor = it
                Schedulers.from(it)
            }
        }
    }
}
