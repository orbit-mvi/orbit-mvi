package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.View
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction
import com.google.android.material.tabs.TabLayout

internal class TabLayoutDetector : InteractionDetector {

    override fun detect(view: View): Sequence<PendingInteraction> {
        if (view is TabLayout) {
            return view.tabSelectedListeners.asSequence().flatMap { tabSelectedListener ->
                view.tabs.flatMap { tab ->
                    val description = "${tab.text} - Tab"
                    sequenceOf(
                        PendingInteraction(
                            source = description,
                            event = "tab selected",
                            executor = {
                                tabSelectedListener.onTabSelected(tab)
                            }
                        ),
                        PendingInteraction(
                            source = description,
                            event = "tab reselected",
                            executor = {
                                tabSelectedListener.onTabReselected(tab)
                            }
                        ),
                        PendingInteraction(
                            source = description,
                            event = "tab unselected",
                            executor = {
                                tabSelectedListener.onTabUnselected(tab)
                            }
                        ))
                }
            }
        }

        return emptySequence()
    }

    private val TabLayout.tabSelectedListeners: List<TabLayout.OnTabSelectedListener>
        @Suppress("UNCHECKED_CAST")
        get() = TabLayout::class.java.getDeclaredField("selectedListeners").apply {
            isAccessible = true
        }.get(this) as? List<TabLayout.OnTabSelectedListener> ?: emptyList()

    private val TabLayout.tabs: Sequence<TabLayout.Tab>
        get() {
            var index = 0
            return generateSequence { getTabAt(index++) }
        }
}
