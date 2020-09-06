package com.babylon.orbit2.uitest.plugin.captor.interaction

import kotlin.reflect.KClass

internal class InteractionRecordExtractor(
    private val screenClass: KClass<out Any>,
    private val interactionRecorder: InteractionRecorder,
    private val lifecycleInteractionRecorder: LifecycleInteractionRecorder
) {

    fun extract(pendingInteraction: PendingInteraction): InteractionRecord? {
        pendingInteraction.executor()
        return interactionRecorder.flush(screenClass)
            .prepareRecord(
                pendingInteraction.source,
                pendingInteraction.event
            )
    }

    fun extractMiddleware(): InteractionRecord? {
        return interactionRecorder.flushMiddlewareLifecycle(screenClass)
            .prepareRecord(
                "Middleware",
                "lifecycle action - created"
            )
    }

    fun extractLifecycle(): List<InteractionRecord> {
        return lifecycleInteractionRecorder.flush(screenClass).mapNotNull { entry ->
            entry.value
                .prepareRecord(
                    screenClass.simpleName.orEmpty(),
                    entry.key
                )
        }
    }

    private fun List<OrbitInteraction>.prepareRecord(source: String, event: String): InteractionRecord? =
        this.map {
            Interaction(
                it.flow,
                it.action.toSanitizedString()
            )
        }
            .takeIf { it.isNotEmpty() } // Filter away empty interactions
            ?.sortedWith(
                compareBy<Interaction> { it.action }
                    .thenBy { it.flow }
            )
            ?.let {
                InteractionRecord(
                    source,
                    event,
                    it
                )
            }
}

internal fun Any.toSanitizedString(): String {
    val simpleName = this::class.simpleName.orEmpty()
    val instance = if (this::class.objectInstance != null) simpleName else toString()
    val nestedHierarchy = this::class.toString().substringAfterLast(".").replace("$", ".")
    return instance.replace(simpleName, nestedHierarchy)
}
