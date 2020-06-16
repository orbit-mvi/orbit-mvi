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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext

object OrbitRxJava2Plugin : OrbitPlugin {
    @ExperimentalCoroutinesApi
    @Suppress("UNCHECKED_CAST")
    @FlowPreview
    override fun <S : Any, E : Any, SE : Any> apply(
        containerContext: OrbitPlugin.ContainerContext<S, SE>,
        flow: Flow<E>,
        operator: Operator<S, E>,
        createContext: (event: E) -> Context<S, E>
    ): Flow<Any> {
        return when (operator) {
            is RxJava2Observable<*, *, *> -> flow.flatMapConcat {
                with(operator as RxJava2Observable<S, E, Any>) {
                    createContext(it).block()
                }.asFlow().flowOn(containerContext.backgroundDispatcher)
            }
            is RxJava2Single<*, *, *> -> flow.map {
                with(operator as RxJava2Single<S, E, Any>) {
                    withContext(containerContext.backgroundDispatcher) {
                        createContext(it).block().await()
                    }
                }
            }
            is RxJava2Maybe<*, *, *> -> flow.mapNotNull {
                with(operator as RxJava2Maybe<S, E, Any>) {
                    withContext(containerContext.backgroundDispatcher) {
                        createContext(it).block().await()
                    }
                }
            }
            is RxJava2Completable -> flow.onEach {
                with(operator) {
                    withContext(containerContext.backgroundDispatcher) {
                        createContext(it).block().await()
                    }
                }
            }
            else -> flow
        }
    }
}
