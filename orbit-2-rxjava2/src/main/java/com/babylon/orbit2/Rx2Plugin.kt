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
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext

internal class RxJava2Observable<S : Any, E : Any, E2 : Any>(val block: suspend Context<S, E>.() -> Observable<E2>) :
    Operator<S, E>

internal class RxJava2Single<S : Any, E : Any, E2 : Any>(val block: suspend Context<S, E>.() -> Single<E2>) :
    Operator<S, E>

internal class RxJava2Maybe<S : Any, E : Any, E2 : Any>(val block: suspend Context<S, E>.() -> Maybe<E2>) :
    Operator<S, E>

internal class RxJava2Completable<S : Any, E : Any>(val block: suspend Context<S, E>.() -> Completable) :
    Operator<S, E>

fun <S : Any, SE : Any, E : Any, E2 : Any> Builder<S, SE, E>.transformRx2Observable(
    block: suspend Context<S, E>.() -> Observable<E2>
): Builder<S, SE, E2> {
    Orbit.requirePlugin(
        RxJava2Plugin,
        "transformRxJava2Observable"
    )
    return Builder(
        stack + RxJava2Observable(
            block
        )
    )
}

fun <S : Any, SE : Any, E : Any, E2 : Any> Builder<S, SE, E>.transformRx2Single(
    block: suspend Context<S, E>.() -> Single<E2>
): Builder<S, SE, E2> {
    Orbit.requirePlugin(RxJava2Plugin, "transformRx2Single")
    return Builder(
        stack + RxJava2Single(
            block
        )
    )
}

fun <S : Any, SE : Any, E : Any, E2 : Any> Builder<S, SE, E>.transformRx2Maybe(
    block: suspend Context<S, E>.() -> Maybe<E2>
): Builder<S, SE, E2> {
    Orbit.requirePlugin(RxJava2Plugin, "transformRx2Maybe")
    return Builder(
        stack + RxJava2Maybe(
            block
        )
    )
}

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

object RxJava2Plugin : OrbitPlugin {
    override fun <S : Any, E : Any, SE : Any> apply(
        containerContext: OrbitPlugin.ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        context: (event: E) -> Context<S, E>
    ): Flow<Any> {
        return when (operator) {
            is RxJava2Observable<*, *, *> -> flow.flatMapConcat {
                with(operator as RxJava2Observable<S, E, Any>) {
                    context(it).block()
                }.asFlow().flowOn(containerContext.backgroundDispatcher)
            }
            is RxJava2Single<*, *, *> -> flow.map {
                with(operator as RxJava2Single<S, E, Any>) {
                    withContext(containerContext.backgroundDispatcher) {
                        context(it).block().await()
                    }
                }
            }
            is RxJava2Maybe<*, *, *> -> flow.mapNotNull {
                with(operator as RxJava2Maybe<S, E, Any>) {
                    withContext(containerContext.backgroundDispatcher) {
                        context(it).block().await()
                    }
                }
            }
            is RxJava2Completable -> flow.onEach {
                with(operator) {
                    withContext(containerContext.backgroundDispatcher) {
                        context(it).block().await()
                    }
                }
            }
            else -> flow
        }
    }
}

fun <T> Stream<T>.asRxObservable() =
    Observable.create<T> { emitter ->
        val closeable = observe {
            if (!emitter.isDisposed) {
                emitter.onNext(it)
            }
        }
        emitter.setCancellable { closeable.close() }
    }
