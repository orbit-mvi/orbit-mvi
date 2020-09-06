package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.View
import androidx.core.view.children
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction
import com.babylon.orbit2.uitest.plugin.captor.interaction.description
import com.google.android.material.bottomnavigation.BottomNavigationView

internal class BottomNavigationViewDetector : InteractionDetector {

    override fun detect(view: View): Sequence<PendingInteraction> {
        return if (view is BottomNavigationView) {

            val selectedInteractions = view.selectedListener?.let {
                view.menu.children.map { menuItem ->
                    PendingInteraction(
                        source = view.description,
                        event = "navigation item selected",
                        executor = {
                            it.onNavigationItemSelected(menuItem)
                        }
                    )
                }
            }

            val reselectedInteractions = view.reselectedListener?.let {
                view.menu.children.map { menuItem ->
                    PendingInteraction(
                        source = view.description,
                        event = "navigation item reselected",
                        executor = {
                            it.onNavigationItemReselected(menuItem)
                        }
                    )
                }
            }

            return sequence {
                selectedInteractions?.let { yieldAll(selectedInteractions) }
                reselectedInteractions?.let { yieldAll(reselectedInteractions) }
                yieldAll(view.menu.pendingInteractions)
            }
        } else {
            emptySequence()
        }
    }

    private val BottomNavigationView.selectedListener: BottomNavigationView.OnNavigationItemSelectedListener?
        get() = BottomNavigationView::class.java.getDeclaredField("selectedListener").apply {
            isAccessible = true
        }.get(this) as? BottomNavigationView.OnNavigationItemSelectedListener

    private val BottomNavigationView.reselectedListener: BottomNavigationView.OnNavigationItemReselectedListener?
        get() = BottomNavigationView::class.java.getDeclaredField("reselectedListener").apply {
            isAccessible = true
        }.get(this) as? BottomNavigationView.OnNavigationItemReselectedListener
}
