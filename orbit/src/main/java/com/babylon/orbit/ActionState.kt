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
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.Executors

data class ActionState<out STATE : Any, out ACTION : Any>(
    val inputState: STATE,
    val action: ACTION
)

fun <STATE : Any, EVENT : Any> Observable<ActionState<STATE, *>>.buildOrbit(
    middleware: Middleware<STATE, EVENT>,
    inputRelay: PublishSubject<Any>
): Observable<STATE> {
    val scheduler = createSingleScheduler()
    return this
        .observeOn(scheduler)
        .publish { actions ->
            Observable.merge(
                middleware.orbits.map { transformer -> transformer(actions, inputRelay) }
            )
        }
        .scan(middleware.initialState) { currentState, partialReducer -> partialReducer(currentState) }
        .distinctUntilChanged()
}

private fun createSingleScheduler(): Scheduler {
    return Schedulers.from(Executors.newSingleThreadExecutor())
}
