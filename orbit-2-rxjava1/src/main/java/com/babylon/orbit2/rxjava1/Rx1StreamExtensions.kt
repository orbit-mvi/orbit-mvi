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

package com.babylon.orbit2.rxjava1

import com.babylon.orbit2.Stream
import rx.Observable
import rx.Subscription
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Consume a [Stream] as an RxJava 1 [Observable].
 */
@Deprecated(
    message = "Stream is deprecated. Please consider upgrading to RxJava 2 or 3 or using Container.stateFlow or Container.sideEffectFlow.",
)
fun <T> Stream<T>.asRx1Observable() = Observable.unsafeCreate<T> { emitter ->
    val unsubscribed = AtomicBoolean(false)
    val closeable = observe {
        if (!emitter.isUnsubscribed) {
            emitter.onNext(it)
        }
    }
    emitter.add(
        object : Subscription {
            override fun isUnsubscribed() = unsubscribed.get()

            override fun unsubscribe() {
                unsubscribed.set(true)
                closeable.close()
                emitter.onCompleted()
            }
        }
    )
}
