package com.babylon.orbit

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
class ConfigReceiver(
    var sideEffectCachingEnabled: Boolean = true
)

@OrbitDsl
open class OrbitsBuilder<STATE : Any, SIDE_EFFECT : Any>(private val initialState: STATE) {
    private val orbits =
        mutableMapOf<String, OrbitContext<STATE, SIDE_EFFECT>.() -> Observable<*>>()
    private val descriptions = mutableSetOf<String>()

    private val config = ConfigReceiver()

    fun configuration(block: ConfigReceiver.() -> Unit) {
        config.apply { block() }
    }

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

    @OrbitDsl
    inner class Transformer<EVENT : Any>(
        private val description: String,
        private val upstreamTransformer: OrbitContext<STATE, SIDE_EFFECT>.() -> Observable<EVENT>
    ) {
        fun <T : Any> transform(transformer: TransformerReceiver<STATE, EVENT>.() -> Observable<T>) =
            this@OrbitsBuilder.Transformer(description) {
                val newContext = switchContextIfNeeded()
                val upstream = if (this != newContext) {
                    upstreamTransformer().observeOn(Schedulers.io())
                } else upstreamTransformer()

                with(newContext) {
                    TransformerReceiver(
                        currentStateProvider,
                        upstream
                    ).transformer()
                }
            }
                .also { this@OrbitsBuilder.orbits[description] = it.upstreamTransformer }

        fun sideEffect(sideEffect: SideEffectEventReceiver<STATE, EVENT, SIDE_EFFECT>.() -> Unit) =
            doOnNextTransformer { event ->
                SideEffectEventReceiver(
                    currentStateProvider,
                    sideEffectSubject,
                    event
                ).sideEffect()
            }

        fun <T : Any> loopBack(mapper: EventReceiver<STATE, EVENT>.() -> T) =
            doOnNextTransformer { event ->
                inputSubject.onNext(
                    EventReceiver(
                        currentStateProvider,
                        event
                    ).mapper()
                )
            }

        fun withReducer(reducer: EventReceiver<STATE, EVENT>.() -> STATE) =
            doOnNextTransformer { event ->
                reducerSubject.onNext { state ->
                    EventReceiver({ state }, event).reducer()
                }
            }

        private fun doOnNextTransformer(func: OrbitContext<STATE, SIDE_EFFECT>.(EVENT) -> Unit) =
            this@OrbitsBuilder.Transformer(
                description
            ) {
                upstreamTransformer()
                    .doOnNext {
                        func(it)
                    }
            }.also { this@OrbitsBuilder.orbits[description] = it.upstreamTransformer }

        private fun OrbitContext<STATE, SIDE_EFFECT>.switchContextIfNeeded(): OrbitContext<STATE, SIDE_EFFECT> {
            return if (ioScheduled) this
            else copy(ioScheduled = true)
        }
    }

    fun build() = object : Middleware<STATE, SIDE_EFFECT> {
        override val configuration = Middleware.Config(
            sideEffectCachingEnabled = config.sideEffectCachingEnabled
        )
        override val initialState: STATE = this@OrbitsBuilder.initialState
        override val orbits: List<TransformerFunction<STATE, SIDE_EFFECT>> =
            this@OrbitsBuilder.orbits.values.toList()
    }
}

@OrbitDsl
class TransformerReceiver<STATE : Any, EVENT : Any>(
    private val stateProvider: () -> STATE,
    val eventObservable: Observable<EVENT>
) {
    fun getCurrentState() = stateProvider()
}

@OrbitDsl
class EventReceiver<STATE : Any, EVENT : Any>(
    private val stateProvider: () -> STATE,
    val event: EVENT
) {
    fun getCurrentState() = stateProvider()
}

@OrbitDsl
class SideEffectEventReceiver<STATE : Any, EVENT : Any, SIDE_EFFECT : Any>(
    private val stateProvider: () -> STATE,
    private val sideEffectRelay: Subject<SIDE_EFFECT>,
    val event: EVENT
) {
    fun getCurrentState() = stateProvider()
    fun post(sideEffect: SIDE_EFFECT) = sideEffectRelay.onNext(sideEffect)
}
