package com.babylon.orbit2.uitest.plugin.captor.interaction

import com.babylon.orbit.LifecycleAction
import kotlin.reflect.KClass

class InteractionRecorderImpl : InteractionRecorder {
    private val ignoredFlows = setOf(
        "_internal capture side effect subject"
    )

    private val recordedInteractions = mutableMapOf<Any, List<OrbitInteraction>>()
    private val recordedMiddlewareInteractions = mutableMapOf<Any, List<OrbitInteraction>>()

    override fun record(screenClass: KClass<out Any>, interaction: OrbitInteraction) {
        if (interaction.flow !in ignoredFlows) {
            val targetMap = when (interaction.action) {
                is LifecycleAction.Created -> recordedMiddlewareInteractions
                else -> recordedInteractions
            }

            targetMap[screenClass] = targetMap[screenClass]?.let { it + interaction } ?: listOf(interaction)
        }
    }

    override fun flush(screenClass: KClass<out Any>): List<OrbitInteraction> =
        recordedInteractions.flush(screenClass)

    override fun flushMiddlewareLifecycle(screenClass: KClass<out Any>): List<OrbitInteraction> =
        recordedMiddlewareInteractions.flush(screenClass)

    private fun MutableMap<Any, List<OrbitInteraction>>.flush(screenClass: KClass<out Any>): List<OrbitInteraction> =
        remove(screenClass).orEmpty()
}
