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

package com.babylon.orbit2

import io.reactivex.Completable

internal class RxJava2Completable<S : Any, E : Any>(val block: suspend Context<S, E>.() -> Completable) :
    Operator<S, E>

fun <S : Any, SE : Any, E : Any> Builder<S, SE, E>.transformRx2Completable(
    block: suspend Context<S, E>.() -> Completable
): Builder<S, SE, E> {
    Orbit.requirePlugin(RxJava2Plugin, "transformRx2Completable")
    return Builder(
        stack + RxJava2Completable(
            block
        )
    )
}
