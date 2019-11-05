package com.babylon.orbit

import hu.akarnokd.rxjava2.subjects.UnicastWorkSubject
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
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
        this@OrbitsBuilder.Transformer<ActionState<STATE, ACTION>>(description, false) { it.ofActionType() }

    @Suppress("unused") // Used for the nice extension function highlight
    fun ActionFilter.on(vararg classes: Class<*>) = this@OrbitsBuilder.Transformer(description, false) { actions ->
        Observable.merge(
            classes.map { clazz -> actions.filter { clazz.isInstance(it.action) } }
        )
    }

    @OrbitDsl
    inner class ActionFilter(val description: String) {

        inline fun <reified ACTION : Any> Observable<ActionState<STATE, *>>.ofActionType(): Observable<ActionState<STATE, ACTION>> =
            filter { it.action is ACTION }
                .cast<ActionState<STATE, ACTION>>()
                .doOnNext { } // TODO logging the flow description
    }

    private  val inProgress = mutableMapOf<String, Transformer<*>>()

    @OrbitDsl
    inner class Transformer<EVENT : Any>(
        private val description: String,
        private val ioScheduled: Boolean,
        private val upstreamTransformer: (Observable<ActionState<STATE, *>>) -> Observable<EVENT>
    ) {

        fun <T : Any> transform(transformer: Observable<EVENT>.() -> Observable<T>) =
            this@OrbitsBuilder.Transformer(description, true) { rawActions ->
                val actions = if(ioScheduled) rawActions else rawActions.observeOn(Schedulers.io())
                transformer(upstreamTransformer(actions))
            }
                .also { this@OrbitsBuilder.inProgress[description] = it }

        fun sideEffect(sideEffect: SideEffectEventReceiver<EVENT, SIDE_EFFECT>.() -> Unit) =
            this@OrbitsBuilder.Transformer(description, false) { rawActions ->
                upstreamTransformer(rawActions)
                    .doOnNext {
                        SideEffectEventReceiver(this@OrbitsBuilder.sideEffectSubject, it).sideEffect()
                    }
            }
                .also { this@OrbitsBuilder.inProgress[description] = it }

        fun <T : Any> loopBack(mapper: EventReceiver<EVENT>.() -> T) {
            this@OrbitsBuilder.inProgress.remove(description)
            this@OrbitsBuilder.orbits += { upstream, inputRelay ->
                upstreamTransformer(upstream)
                    .doOnNext { action -> inputRelay.onNext(EventReceiver(action).mapper()) }
                    .map {
                        { state: STATE -> state }
                    }
            }
        }

        fun withReducer(reducer: ReducerReceiver<STATE, EVENT>.() -> STATE) {
            this@OrbitsBuilder.inProgress.remove(description)
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
        init {
            ArrayList(inProgress.values).forEach { transformer -> transformer.withReducer { currentState } }
        }
        override val initialState: STATE = this@OrbitsBuilder.initialState
        override val orbits: List<TransformerFunction<STATE>> = this@OrbitsBuilder.orbits
        override val sideEffect: Observable<SIDE_EFFECT> = sideEffectSubject.hide()
    }
}

class ReducerReceiver<STATE : Any, EVENT : Any>(
    val currentState: STATE,
    val event: EVENT
)

class EventReceiver<EVENT : Any>(
    val event: EVENT
)

class SideEffectEventReceiver<EVENT : Any, SIDE_EFFECT: Any>(
    private val sideEffectRelay: Subject<SIDE_EFFECT>,
    val event: EVENT
) {
    fun post(sideEffect: SIDE_EFFECT) = sideEffectRelay.onNext(sideEffect)
}
