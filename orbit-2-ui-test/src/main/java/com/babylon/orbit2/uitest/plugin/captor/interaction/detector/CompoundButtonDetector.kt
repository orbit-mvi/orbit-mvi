package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.View
import android.widget.CompoundButton
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction
import com.babylon.orbit2.uitest.plugin.captor.interaction.description

internal class CompoundButtonDetector : InteractionDetector {

    override fun detect(view: View): Sequence<PendingInteraction> {
        if (view is CompoundButton) {
            view.checkedChangeListener?.let { checkedChangeListener ->
                return sequenceOf(
                    PendingInteraction(
                        source = view.description,
                        event = "checked change - checked",
                        executor = {
                            checkedChangeListener.onCheckedChanged(view, true)
                        }
                    ),
                    PendingInteraction(
                        source = view.description,
                        event = "checked change - unchecked",
                        executor = {
                            checkedChangeListener.onCheckedChanged(view, false)
                        }
                    ))
            }
        }

        return emptySequence()
    }

    private val CompoundButton.checkedChangeListener: CompoundButton.OnCheckedChangeListener?
        get() = CompoundButton::class.java.getDeclaredField("mOnCheckedChangeListener").apply {
            isAccessible = true
        }.get(this) as? CompoundButton.OnCheckedChangeListener
}
