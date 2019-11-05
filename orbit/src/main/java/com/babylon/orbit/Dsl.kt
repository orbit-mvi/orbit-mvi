package com.babylon.orbit

import hu.akarnokd.rxjava2.subjects.UnicastWorkSubject
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
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
class ActionFilter(val description: String)

@OrbitDsl
open class OrbitsBuilder<STATE : Any, SIDE_EFFECT : Any>(private val initialState: STATE) {
    // Since this caches unconsumed events we restrict it to one subscriber at a time
    protected val sideEffectSubject: Subject<SIDE_EFFECT> = UnicastWorkSubject.create()

    private val orbits = mutableListOf<TransformerFunction<STATE>>()

    @Suppress("unused") // Used for the nice extension function highlight
    fun OrbitsBuilder<STATE, SIDE_EFFECT>.perform(description: String) = ActionFilter(description)
        .also {
            require(!descriptions.contains(description)) {
                "Names used in perform must be unique! $description already exists!"
            }
            descriptions.add(description)
        }

    inline fun <reified ACTION : Any> ActionFilter.on() =
        this@OrbitsBuilder.Transformer<ACTION>(description) { rawActions.ofType() }

    @Suppress("unused") // Used for the nice extension function highlight
    fun ActionFilter.on(vararg classes: Class<*>) =
        this@OrbitsBuilder.Transformer(description) {
            Observable.merge(
                classes.map { clazz -> rawActions.filter { clazz.isInstance(it) } }
            )
        }

    private val inProgress = mutableMapOf<String, OrbitContext<STATE>.() -> Observable<*>>()
    private val descriptions = mutableSetOf<String>()

    @OrbitDsl
    inner class Transformer<EVENT : Any>(
        private val description: String,
        private val upstreamTransformer: OrbitContext<STATE>.() -> Observable<EVENT>
    ) {
        fun <T : Any> transform(transformer: TransformerReceiver<STATE, EVENT>.() -> Observable<T>) =
            this@OrbitsBuilder.Transformer(description) {
                with(switchContextIfNeeded()) {
                    TransformerReceiver(
                        currentStateProvider,
                        upstreamTransformer()
                    ).transformer()
                }
            }
                .also { this@OrbitsBuilder.inProgress[description] = it.upstreamTransformer }

        fun sideEffect(sideEffect: SideEffectEventReceiver<STATE, EVENT, SIDE_EFFECT>.() -> Unit) =
            this@OrbitsBuilder.Transformer(
                description
            ) {
                upstreamTransformer()
                    .doOnNext {
                        SideEffectEventReceiver(
                            currentStateProvider,
                            this@OrbitsBuilder.sideEffectSubject,
                            it
                        ).sideEffect()
                    }
            }
                .also { this@OrbitsBuilder.inProgress[description] = it.upstreamTransformer }

        fun <T : Any> loopBack(mapper: EventReceiver<STATE, EVENT>.() -> T) =
            this@OrbitsBuilder.Transformer(
                description
            ) {
                upstreamTransformer()
                    .doOnNext { action ->
                        inputRelay.onNext(
                            EventReceiver(
                                currentStateProvider,
                                action
                            ).mapper()
                        )
                    }
            }.also { this@OrbitsBuilder.inProgress[description] = it.upstreamTransformer }

        fun withReducer(reducer: EventReceiver<STATE, EVENT>.() -> STATE) =
            this@OrbitsBuilder.Transformer(
                description
            ) {
                upstreamTransformer()
                    .doOnNext {
                        reducerRelay.onNext { state ->
                            EventReceiver({ state }, it).reducer()
                        }
                    }
            }.also { this@OrbitsBuilder.inProgress[description] = it.upstreamTransformer }

        private fun OrbitContext<STATE>.switchContextIfNeeded(): OrbitContext<STATE> {
            return if (ioScheduled) this
            else OrbitContext(
                currentStateProvider,
                rawActions.observeOn(Schedulers.io()),
                inputRelay,
                reducerRelay,
                true
            )
        }
    }

    fun build() = object : Middleware<STATE, SIDE_EFFECT> {
        override val initialState: STATE = this@OrbitsBuilder.initialState
        override val orbits: List<TransformerFunction<STATE>> =
            this@OrbitsBuilder.inProgress.values.toList()
        override val sideEffect: Observable<SIDE_EFFECT> = sideEffectSubject.hide()
    }
}

class TransformerReceiver<STATE : Any, EVENT : Any>(
    private val stateProvider: () -> STATE,
    val eventObservable: Observable<EVENT>
) {
    fun getCurrentState() = stateProvider()
}

class EventReceiver<STATE : Any, EVENT : Any>(
    private val stateProvider: () -> STATE,
    val event: EVENT
) {
    fun getCurrentState() = stateProvider()
}

class SideEffectEventReceiver<STATE : Any, EVENT : Any, SIDE_EFFECT : Any>(
    private val stateProvider: () -> STATE,
    private val sideEffectRelay: Subject<SIDE_EFFECT>,
    val event: EVENT
) {
    fun getCurrentState() = stateProvider()
    fun post(sideEffect: SIDE_EFFECT) = sideEffectRelay.onNext(sideEffect)
}
