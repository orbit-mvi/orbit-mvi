package com.babylon.orbit

import hu.akarnokd.rxjava2.subjects.UnicastWorkSubject
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.Subject

/*
What do we want to log:
- which flow was triggered
- by what action & state combination
- any outcome (new state emitted, something looped back etc.)
 */

@DslMarker
annotation class OrbitDsl

fun <STATE : Any, EVENT : Any> middleware(
    initialState: STATE,
    init: OrbitsBuilder<STATE, EVENT>.() -> Unit
): Middleware<STATE, EVENT> {

    return OrbitsBuilder<STATE, EVENT>(initialState).apply {
        init(this)
    }.build()
}

@OrbitDsl
open class OrbitsBuilder<STATE : Any, EVENT : Any>(private val initialState: STATE) {
    // Since this caches unconsumed events we restrict it to one subscriber at a time
    protected val sideEffectSubject: Subject<EVENT> = UnicastWorkSubject.create()

    private val sideEffectRelay: SideEffectRelay<EVENT> = object : SideEffectRelay<EVENT> {
        override fun post(event: EVENT) {
            sideEffectSubject.onNext(event)
        }
    }

    private val orbits = mutableListOf<TransformerFunction<STATE>>()

    @Suppress("unused") // Used for the nice extension function highlight
    fun OrbitsBuilder<STATE, EVENT>.perform(description: String) = ActionFilter(description)

    inline fun <reified ACTION : Any> ActionFilter.on() =
        this@OrbitsBuilder.FirstTransformer<ACTION> { it.ofActionType() }

    @Suppress("unused") // Used for the nice extension function highlight
    fun ActionFilter.on(vararg classes: Class<*>) = this@OrbitsBuilder.FirstTransformer { actions ->
        Observable.merge(
            classes.map { clazz -> actions.filter { clazz.isInstance(it.action) } }
        )
    }

    @OrbitDsl
    inner class ActionFilter(private val description: String) {

        inline fun <reified ACTION : Any> Observable<ActionState<STATE, *>>.ofActionType(): Observable<ActionState<STATE, ACTION>> =
            filter { it.action is ACTION }
                .cast<ActionState<STATE, ACTION>>()
                .doOnNext { } // TODO logging the flow description
    }

    @OrbitDsl
    inner class FirstTransformer<ACTION : Any>(
        private val upstreamTransformer: (Observable<ActionState<STATE, *>>) -> Observable<ActionState<STATE, ACTION>>
    ) {

        fun <T : Any> transform(transformer: Observable<ActionState<STATE, ACTION>>.() -> Observable<T>) =
            this@OrbitsBuilder.Transformer { rawActions ->
                transformer(upstreamTransformer(rawActions.observeOn(Schedulers.io())))
            }

        fun sideEffect(sideEffect: (SideEffectRelay<EVENT>, ActionState<STATE, ACTION>) -> Unit) =
            this@OrbitsBuilder.Transformer { rawActions ->
                upstreamTransformer(rawActions.observeOn(Schedulers.io()))
                    .doOnNext {
                        sideEffect(this@OrbitsBuilder.sideEffectRelay, it)
                    }
            }

        fun sideEffect(sideEffect: (SideEffectRelay<EVENT>) -> Unit) =
            this@OrbitsBuilder.Transformer { rawActions ->
                upstreamTransformer(rawActions.observeOn(Schedulers.io()))
                    .doOnNext {
                        sideEffect(this@OrbitsBuilder.sideEffectRelay)
                    }
            }

        fun sideEffect(sideEffect: () -> Unit) =
            this@OrbitsBuilder.Transformer { rawActions ->
                upstreamTransformer(rawActions.observeOn(Schedulers.io()))
                    .doOnNext {
                        sideEffect()
                    }
            }

        fun withReducer(reducer: (STATE, ACTION) -> STATE) {
            this@OrbitsBuilder.orbits += { rawActions, _ ->
                upstreamTransformer(rawActions)
                    .map { it.action }
                    .map {
                        { state: STATE ->
                            reducer(state, it)
                        }
                    }
            }
        }

        fun withReducer(reducer: (STATE) -> STATE) {
            this@OrbitsBuilder.orbits += { rawActions, _ ->
                upstreamTransformer(rawActions)
                    .map {
                        { state: STATE ->
                            reducer(state)
                        }
                    }
            }
        }
    }

    @OrbitDsl
    inner class Transformer<ACTION : Any>(private val upstreamTransformer: (Observable<ActionState<STATE, *>>) -> Observable<ACTION>) {

        fun <T : Any> transform(transformer: Observable<ACTION>.() -> Observable<T>) =
            this@OrbitsBuilder.Transformer { rawActions ->
                transformer(upstreamTransformer(rawActions))
            }

        fun sideEffect(sideEffect: (SideEffectRelay<EVENT>, ACTION) -> Unit) =
            this@OrbitsBuilder.Transformer { rawActions ->
                upstreamTransformer(rawActions)
                    .doOnNext {
                        sideEffect(this@OrbitsBuilder.sideEffectRelay, it)
                    }
            }

        fun sideEffect(sideEffect: (SideEffectRelay<EVENT>) -> Unit) =
                this@OrbitsBuilder.Transformer { rawActions ->
                    upstreamTransformer(rawActions)
                            .doOnNext {
                                sideEffect(this@OrbitsBuilder.sideEffectRelay)
                            }
                }

        fun sideEffect(sideEffect: () -> Unit) =
                this@OrbitsBuilder.Transformer { rawActions ->
                    upstreamTransformer(rawActions)
                            .doOnNext {
                                sideEffect()
                            }
                }

        fun <T : Any> loopBack(mapper: (ACTION) -> T) {
            this@OrbitsBuilder.orbits += { upstream, inputRelay ->
                upstreamTransformer(upstream)
                    .doOnNext { action -> inputRelay.accept(mapper(action)) }
                    .map {
                        { state: STATE -> state }
                    }
            }
        }

        fun ignoringEvents() {
            this@OrbitsBuilder.orbits += { upstream, _ ->
                upstreamTransformer(upstream)
                    .map {
                        { state: STATE -> state }
                    }
            }
        }

        fun withReducer(reducer: (STATE, ACTION) -> STATE) {
            this@OrbitsBuilder.orbits += { rawActions, _ ->
                upstreamTransformer(rawActions)
                    .map {
                        { state: STATE ->
                            reducer(state, it)
                        }
                    }
            }
        }

        fun withReducer(reducer: (STATE) -> STATE) {
            this@OrbitsBuilder.orbits += { rawActions, _ ->
                upstreamTransformer(rawActions)
                    .map {
                        { state: STATE ->
                            reducer(state)
                        }
                    }
            }
        }
    }

    fun build() = object : Middleware<STATE, EVENT> {
        override val initialState: STATE = this@OrbitsBuilder.initialState
        override val orbits: List<TransformerFunction<STATE>> = this@OrbitsBuilder.orbits
        override val sideEffect: Observable<EVENT> = sideEffectSubject.hide()
    }
}
