package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.View

internal open class CompositeDetector(private vararg val detectors: InteractionDetector) : InteractionDetector {

    override fun detect(view: View) = detectors.asSequence().flatMap {
        it.detect(view)
    }
}
