package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.View
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction

internal interface InteractionDetector {
    fun detect(view: View): Sequence<PendingInteraction>
}
