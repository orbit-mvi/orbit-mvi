package com.babylon.orbit2.uitest.plugin.captor.interaction

object InteractionRecorderFactory {
    val interactionRecorder: InteractionRecorder = InteractionRecorderImpl()
    val lifecycleInteractionRecorder: LifecycleInteractionRecorder = LifecycleInteractionRecorderImpl(interactionRecorder)
}
