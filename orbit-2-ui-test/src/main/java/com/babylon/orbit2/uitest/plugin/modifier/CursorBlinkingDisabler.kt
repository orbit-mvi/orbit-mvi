package com.babylon.orbit2.uitest.plugin.modifier

import android.view.View
import android.widget.EditText
import androidx.test.platform.app.InstrumentationRegistry
import com.babylon.orbit2.uitest.plugin.captor.interaction.hierarchySequence

internal class CursorBlinkingDisabler : ViewModifier {

    override fun modify(view: View) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            view.hierarchySequence()
                .filterIsInstance<EditText>()
                .forEach { it.isCursorVisible = false }
        }
    }
}
