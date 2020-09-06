package com.babylon.orbit2.uitest.plugin.captor.interaction

import kotlin.reflect.KClass

interface InteractionRecorder {

    fun record(screenClass: KClass<out Any>, interaction: OrbitInteraction)
    fun flush(screenClass: KClass<out Any>): List<OrbitInteraction>
    fun flushMiddlewareLifecycle(screenClass: KClass<out Any>): List<OrbitInteraction>
}
