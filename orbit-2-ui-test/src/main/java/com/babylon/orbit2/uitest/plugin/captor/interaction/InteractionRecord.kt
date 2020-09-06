package com.babylon.orbit2.uitest.plugin.captor.interaction

internal data class InteractionRecord(
    val source: String,
    val event: String,
    val interactions: List<Interaction>
)

internal data class Interaction(
    val flow: String,
    val action: String
)
