package com.babylon.orbit2.uitest.plugin.captor.interaction

import androidx.lifecycle.LifecycleObserver
import kotlin.reflect.KClass

interface LifecycleInteractionRecorder : LifecycleObserver {
    fun flush(screenClass: KClass<out Any>): Map<String, List<OrbitInteraction>>
}
