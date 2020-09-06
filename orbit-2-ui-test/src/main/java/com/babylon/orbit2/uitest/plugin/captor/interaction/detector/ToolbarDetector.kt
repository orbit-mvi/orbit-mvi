package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction

internal class ToolbarDetector : InteractionDetector {

    override fun detect(view: View): Sequence<PendingInteraction> {
        if (view is Toolbar) {
            // NOTE: setNavigationOnClickListener maps to navButtonView.setOnClickListener so is handled through ViewDetector

            val parentInteraction = view.menuItemClickListener?.let { toolbarMenuItemClickListener ->
                view.menu.children.map { menuItem ->
                    menuItem.pendingInteraction {
                        toolbarMenuItemClickListener.onMenuItemClick(menuItem)
                    }
                }
            }

            return sequence {
                parentInteraction?.let { yieldAll(parentInteraction) }
                yieldAll(view.menu.pendingInteractions)
            }
        }

        return emptySequence()
    }

    private val Toolbar.menuItemClickListener: Toolbar.OnMenuItemClickListener?
        get() = Toolbar::class.java.getDeclaredField("mOnMenuItemClickListener").apply {
            isAccessible = true
        }.get(this) as? Toolbar.OnMenuItemClickListener
}
