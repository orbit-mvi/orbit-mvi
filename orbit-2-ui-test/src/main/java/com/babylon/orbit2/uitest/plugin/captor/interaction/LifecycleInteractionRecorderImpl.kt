package com.babylon.orbit2.uitest.plugin.captor.interaction

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlin.reflect.KClass

class LifecycleInteractionRecorderImpl(private val interactionRecorder: InteractionRecorder) : LifecycleInteractionRecorder {

    private val lifecycleInteractions = mutableMapOf<KClass<out Any>, MutableMap<String, List<OrbitInteraction>>>()

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onAny(screen: LifecycleOwner, event: Lifecycle.Event) {

        val eventString = when (event) {
            Lifecycle.Event.ON_CREATE -> "onCreate"
            Lifecycle.Event.ON_START -> "onStart"
            Lifecycle.Event.ON_RESUME -> "onResume"
            Lifecycle.Event.ON_PAUSE -> "onPause"
            Lifecycle.Event.ON_STOP -> "onStop"
            Lifecycle.Event.ON_DESTROY -> "onDestroy"
            Lifecycle.Event.ON_ANY -> "onAny"
        }

        lifecycleInteractions.getOrPut(screen::class) { mutableMapOf() }[eventString] = interactionRecorder.flush(screen::class)
    }

    override fun flush(screenClass: KClass<out Any>): Map<String, List<OrbitInteraction>> = lifecycleInteractions.remove(screenClass).orEmpty()
}
