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

fun <STATE : Any, SIDE_EFFECT : Any> middleware(
    initialState: STATE,
    init: OrbitsBuilder<STATE, SIDE_EFFECT>.() -> Unit
): Middleware<STATE, SIDE_EFFECT> {

    return OrbitsBuilder<STATE, SIDE_EFFECT>(initialState).apply {
        init(this)
    }.build()
}

@OrbitDsl
open class OrbitsBuilder<STATE : Any, SIDE_EFFECT : Any>(private val initialState: STATE) {
    // Since this caches unconsumed events we restrict it to one subscriber at a time
    protected val sideEffectSubject: Subject<SIDE_EFFECT> = UnicastWorkSubject.create()

    private val orbits = mutableListOf<TransformerFunction<STATE>>()

    @Suppress("unused") // Used for the nice extension function highlight
    fun OrbitsBuilder<STATE, SIDE_EFFECT>.perform(description: String) = ActionFilter(description)

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

        fun postSideEffect(sideEffect: ActionState<STATE, ACTION>.() -> SIDE_EFFECT) =
            sideEffectInternal {
                this@OrbitsBuilder.sideEffectSubject.onNext(
                    it.sideEffect()
                )
            }

        fun sideEffect(sideEffect: ActionState<STATE, ACTION>.() -> Unit) =
            sideEffectInternal {
                it.sideEffect()
            }

        private fun sideEffectInternal(sideEffect: (ActionState<STATE, ACTION>) -> Unit) =
            this@OrbitsBuilder.FirstTransformer { rawActions ->
                upstreamTransformer(rawActions)
                    .doOnNext {
                        sideEffect(it)
                    }
            }

        fun withReducer(reducer: ReducerReceiver<STATE, ACTION>.() -> STATE) {
            this@OrbitsBuilder.orbits += { rawActions, _ ->
                upstreamTransformer(rawActions)
                    .map {
                        { state: STATE ->
                            ReducerReceiver(state, it.action).reducer()
                        }
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
    }

    @OrbitDsl
    inner class Transformer<EVENT : Any>(private val upstreamTransformer: (Observable<ActionState<STATE, *>>) -> Observable<EVENT>) {

        fun <T : Any> transform(transformer: Observable<EVENT>.() -> Observable<T>) =
            this@OrbitsBuilder.Transformer { rawActions ->
                transformer(upstreamTransformer(rawActions))
            }

        fun postSideEffect(sideEffect: EventReceiver<EVENT>.() -> SIDE_EFFECT) =
            sideEffectInternal {
                this@OrbitsBuilder.sideEffectSubject.onNext(EventReceiver(it).sideEffect())
            }

        fun sideEffect(sideEffect: EventReceiver<EVENT>.() -> Unit) =
            sideEffectInternal {
                EventReceiver(it).sideEffect()
            }

        private fun sideEffectInternal(sideEffect: (EVENT) -> Unit) =
            this@OrbitsBuilder.Transformer { rawActions ->
                upstreamTransformer(rawActions)
                    .doOnNext {
                        sideEffect(it)
                    }
            }

        fun <T : Any> loopBack(mapper: EventReceiver<EVENT>.() -> T) {
            this@OrbitsBuilder.orbits += { upstream, inputRelay ->
                upstreamTransformer(upstream)
                    .doOnNext { action -> inputRelay.onNext(EventReceiver(action).mapper()) }
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

        fun withReducer(reducer: ReducerReceiver<STATE, EVENT>.() -> STATE) {
            this@OrbitsBuilder.orbits += { rawActions, _ ->
                upstreamTransformer(rawActions)
                    .map {
                        { state: STATE ->
                            ReducerReceiver(state, it).reducer()
                        }
                    }
            }
        }
    }

    fun build() = object : Middleware<STATE, SIDE_EFFECT> {
        override val initialState: STATE = this@OrbitsBuilder.initialState
        override val orbits: List<TransformerFunction<STATE>> = this@OrbitsBuilder.orbits
        override val sideEffect: Observable<SIDE_EFFECT> = sideEffectSubject.hide()
    }
}

class TransformerReceiver<STATE : Any, EVENT : Any>(
    val inputState: STATE,
    val action: EVENT
)

class ReducerReceiver<STATE : Any, EVENT : Any>(
    val currentState: STATE,
    val event: EVENT
)

class EventReceiver<EVENT : Any>(
    val event: EVENT
)
