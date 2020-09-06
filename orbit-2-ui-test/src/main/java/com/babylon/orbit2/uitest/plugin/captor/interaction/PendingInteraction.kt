package com.babylon.orbit2.uitest.plugin.captor.interaction

internal data class PendingInteraction(
    val source: String,
    val event: String,
    val executor: () -> Unit
)
