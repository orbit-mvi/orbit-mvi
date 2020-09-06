package com.babylon.orbit2.uitest.plugin.modifier

import android.view.View
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Dialogs and views don't always have a background color so we force one from the parent otherwise screenshots end up transparent
 */
internal class BackgroundColorModifier : ViewModifier {
    override fun modify(view: View) {
        if (view.background == null) {
            val decorViewBackground = generateSequence(view) { it.parent as? View }
                .mapNotNull { it.background }
                .firstOrNull()

            if (decorViewBackground != null) {
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    view.background = decorViewBackground
                }
            }
        }
    }
}
