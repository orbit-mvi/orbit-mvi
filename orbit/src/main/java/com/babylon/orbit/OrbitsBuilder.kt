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
import io.reactivex.rxkotlin.ofType

@OrbitDsl
open class OrbitsBuilder<STATE : Any, SIDE_EFFECT : Any>(private val initialState: STATE) {
    private val orbits =
        mutableMapOf<String, OrbitContext<STATE, SIDE_EFFECT>.() -> Observable<*>>()
    private val descriptions = mutableSetOf<String>()

    private val config = ConfigReceiver()

    fun configuration(block: ConfigReceiver.() -> Unit) {
        config.apply { block() }
    }

    /**
     *  `perform` begins a new flow declaration. It needs to be followed by further transformations to
     *  be useful.
     *
     *  The description provided here will be used in debug logging.
     *
     *  @param description The description for your flow. This needs to be unique within the same middleware.
     */
    @Suppress("unused") // Used for the nice extension function highlight
    fun OrbitsBuilder<STATE, SIDE_EFFECT>.perform(description: String) = ActionFilter(
        description
    )
        .also {
            require(!descriptions.contains(description)) {
                "Names used in perform must be unique! $description already exists!"
            }
            descriptions.add(description)
        }

    /**
     * `on` sets up the flow to trigger only on actions of a certain type. Only actions of this type
     * will be emitted downstream.
     *
     * Be careful not to use generic types in this filter! Due to type erasure
     * e.g. `List<Int>` and `List<String>` resolve to the same class, potentially
     * causing unintended events or crashes.
     *
     * @param ACTION The action type to filter on.
     */
    inline fun <reified ACTION : Any> ActionFilter.on() =
        this@OrbitsBuilder.Transformer<ACTION>(description) { rawActions.ofType() }

    /**
     * `on` sets up the flow to trigger only on actions of certain types. Only actions of these types
     * will be emitted downstream.
     *
     * Be careful not to use generic types in this filter! Due to type erasure
     * e.g. `List<Int>` and `List<String>` resolve to the same class, potentially
     * causing unintended events or crashes.
     *
     * @param classes The action types to filter on.
     */
    @Suppress("unused") // Used for the nice extension function highlight
    fun ActionFilter.on(vararg classes: Class<*>) =
        this@OrbitsBuilder.Transformer(description) {
            Observable.merge(
                classes.map { clazz -> rawActions.filter { clazz.isInstance(it) } }
            )
        }

    @OrbitDsl
    inner class Transformer<EVENT : Any>(
        private val description: String,
        private val upstreamTransformer: OrbitContext<STATE, SIDE_EFFECT>.() -> Observable<EVENT>
    ) {

        /**
         * Transform allows you to apply a series of RxJava operators in order to transform the original
         * to return some other type. Typically you would flatmap or compose the original with use cases
         * in the form of observables or observable transformers.
         *
         * The first transformer you invoke switches the downstream of the original observable to an
         * IO scheduler.
         *
         * @param transformer the lambda applying the transformation
         */
        fun <T : Any> transform(transformer: TransformerReceiver<STATE, EVENT>.() -> Observable<T>) =
            this@OrbitsBuilder.Transformer(description) {
                TransformerReceiver(
                    currentStateProvider,
                    upstreamTransformer().observeOn(backgroundScheduler)
                ).transformer()
            }
                .also { this@OrbitsBuilder.orbits[description] = it.upstreamTransformer }

        /**
         * Side effects allow you to deal with things like tracking, navigation etc.
         *
         * There is also a special type of side effects - ones that are meant for the view to listen
         * to as one-off events that are awkward to represent as part of the state - typically things
         * like navigation, showing transient views like toasts etc.
         *
         * These are delivered through the side effect relay available through [OrbitContainer.sideEffect]
         * or OrbitViewModel.sideEffect.
         *
         * Side effects are passthrough transformers. This means that after applying
         * a side effect, the upstream events are passed through unmodified.
         *
         * @param sideEffect the lambda applying the side effect
         */
        fun sideEffect(sideEffect: SideEffectEventReceiver<STATE, EVENT, SIDE_EFFECT>.() -> Unit) =
            doOnNextTransformer { event ->
                SideEffectEventReceiver(
                    currentStateProvider,
                    sideEffectSubject,
                    event
                ).sideEffect()
            }

        /**
         * Loopbacks allow you to loop the upstream events back into the MVI system input. This allows
         * you to effectively split your event stream into multiple ones that can apply different
         * logic.
         *
         * It is recommended to put the event into a wrapper when doing so - for transparency as well
         * as avoiding any type erasure related issues on the action filters.
         *
         * The provided lambda has to return the event you wish to loop back.
         *
         * Loopbacks are passthrough transformers. This means that after applying
         * a loopback, the upstream events are passed through unmodified.
         *
         * @param mapper the lambda mapping the incoming event to the looped back event.
         */
        fun <T : Any> loopBack(mapper: EventReceiver<STATE, EVENT>.() -> T) =
            doOnNextTransformer { event ->
                inputSubject.onNext(
                    EventReceiver(
                        currentStateProvider,
                        event
                    ).mapper()
                )
            }

        /**
         * Reducers reduce the current state and incoming events to produce a new state.
         *
         * Downstream transformers await for the state to be reduced.
         *
         * Loopbacks are passthrough transformers. This means that after applying
         * a loopback, the upstream events are passed through unmodified.
         *
         * @param reducer the lambda reducing the current state and incoming event to produce a new state
         */
        fun reduce(reducer: EventReceiver<STATE, EVENT>.() -> STATE) =
            this@OrbitsBuilder.Transformer(
                description
            ) {
                upstreamTransformer()
                    .flatMapSingle { event ->
                        reduce { state ->
                            EventReceiver(
                                { state },
                                event
                            ).reducer()
                        }.map { event } // To be removed to make reducers emit the state
                    }
                    .observeOn(backgroundScheduler)
            }.also { this@OrbitsBuilder.orbits[description] = it.upstreamTransformer }

        private fun doOnNextTransformer(func: OrbitContext<STATE, SIDE_EFFECT>.(EVENT) -> Unit) =
            this@OrbitsBuilder.Transformer(
                description
            ) {
                upstreamTransformer()
                    .doOnNext {
                        func(it)
                    }
            }.also { this@OrbitsBuilder.orbits[description] = it.upstreamTransformer }
    }

    fun build() = object : Middleware<STATE, SIDE_EFFECT> {
        override val configuration = Middleware.Config(
            sideEffectCachingEnabled = config.sideEffectCachingEnabled
        )
        override val initialState: STATE = this@OrbitsBuilder.initialState
        override val orbits: Map<String, TransformerFunction<STATE, SIDE_EFFECT>> =
            this@OrbitsBuilder.orbits
    }
}

/**
 * Convenience method for creating a [Middleware].
 *
 * @param initialState The initial state to set on your MVI system.
 * @param init The DSL implementation describing your MVI flows.
 */
fun <STATE : Any, SIDE_EFFECT : Any> middleware(
    initialState: STATE,
    init: OrbitsBuilder<STATE, SIDE_EFFECT>.() -> Unit
): Middleware<STATE, SIDE_EFFECT> {

    return OrbitsBuilder<STATE, SIDE_EFFECT>(initialState).apply {
        init(this)
    }.build()
}
