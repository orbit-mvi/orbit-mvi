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
import io.reactivex.subjects.PublishSubject

typealias TransformerFunction<STATE, SIDE_EFFECT> = OrbitContext<STATE, SIDE_EFFECT>.() -> (Observable<*>)

data class OrbitContext<STATE : Any, SIDE_EFFECT : Any>(
    val currentStateProvider: () -> STATE,
    val rawActions: Observable<*>,
    val inputSubject: PublishSubject<Any>,
    val reduce: ((STATE) -> STATE) -> Single<STATE>,
    val sideEffectSubject: PublishSubject<SIDE_EFFECT>
)
