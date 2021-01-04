/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.rxjava1

import org.orbitmvi.orbit.syntax.Operator
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugin
import org.orbitmvi.orbit.syntax.strict.VolatileContext
import org.orbitmvi.orbit.idling.withIdling
import org.orbitmvi.orbit.idling.withIdlingFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import rx.Completable
import rx.CompletableSubscriber
import rx.Observable
import rx.Observer
import rx.Single
import rx.SingleSubscriber
import rx.Subscription
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Orbit plugin providing RxJava 1 DSL operators:
 *
 * * [transformRx1Observable]
 * * [transformRx1Single]
 * * [transformRx1Completable]
 */
public object RxJava1DslPlugin : OrbitDslPlugin {

    @Suppress("UNCHECKED_CAST", "EXPERIMENTAL_API_USAGE")
    override fun <S : Any, E, SE : Any> apply(
        containerContext: OrbitDslPlugin.ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        createContext: (event: E) -> VolatileContext<S, E>
    ): Flow<Any?> {
        return when (operator) {
            is RxJava1Observable<*, *, *> -> flow.flatMapConcat {
                containerContext.withIdlingFlow(operator as RxJava1Observable<S, E, Any?>) {
                    createContext(it).block().asFlow().flowOn(containerContext.settings.backgroundDispatcher)
                }
            }
            is RxJava1Single<*, *, *> -> flow.map {
                containerContext.withIdling(operator as RxJava1Single<S, E, Any?>) {
                    withContext(containerContext.settings.backgroundDispatcher) {
                        createContext(it).block().await()
                    }
                }
            }
            is RxJava1Completable -> flow.onEach {
                containerContext.withIdling(operator) {
                    withContext(containerContext.settings.backgroundDispatcher) {
                        createContext(it).block().suspendAwait()
                    }
                }
            }
            else -> flow
        }
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
private fun <T> Observable<T>.asFlow(): Flow<T> = callbackFlow {
    val observer = object : Observer<T> {
        override fun onError(e: Throwable?) {
            close(e)
        }

        override fun onNext(t: T) {
            sendBlocking(t)
        }

        override fun onCompleted() {
            close()
        }
    }

    val subscription = subscribe(observer)
    awaitClose { subscription.unsubscribe() }
}

private suspend fun <T> Single<T>.await(): T = suspendCancellableCoroutine { cont ->
    val subscription = subscribe(
        object : SingleSubscriber<T>() {
            override fun onSuccess(t: T) = cont.resume(t)
            override fun onError(error: Throwable) = cont.resumeWithException(error)
        }
    )
    cont.invokeOnCancellation { subscription.unsubscribe() }
}

private suspend fun Completable.suspendAwait(): Unit = suspendCancellableCoroutine { cont ->
    subscribe(
        object : CompletableSubscriber {
            override fun onSubscribe(d: Subscription) = cont.invokeOnCancellation { d.unsubscribe() }
            override fun onCompleted() = cont.resume(Unit)
            override fun onError(e: Throwable) = cont.resumeWithException(e)
        }
    )
}
