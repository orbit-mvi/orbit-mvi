package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.View
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction
import com.babylon.orbit2.uitest.plugin.captor.interaction.description

internal class ViewPagerDetector : InteractionDetector {

    override fun detect(view: View): Sequence<PendingInteraction> {
        if (view is ViewPager) {
            return view.pageChangeListeners.asSequence().flatMap { listener ->
                sequenceOf(
                    PendingInteraction(
                        source = view.description,
                        event = "page scroll state changed",
                        executor = {
                            listener.onPageScrollStateChanged(SCROLL_STATE_IDLE)
                        }
                    ),
                    PendingInteraction(
                        source = view.description,
                        event = "page scrolled",
                        executor = {
                            listener.onPageScrolled(0, 0f, 0)
                        }
                    ),
                    PendingInteraction(
                        source = view.description,
                        event = "page selected",
                        executor = {
                            listener.onPageSelected(0)
                        }
                    )
                )
            }
        }

        return emptySequence()
    }

    private val ViewPager.pageChangeListeners: List<ViewPager.OnPageChangeListener>
        @Suppress("UNCHECKED_CAST")
        get() = ViewPager::class.java.getDeclaredField("mOnPageChangeListeners").apply {
            isAccessible = true
        }.get(this) as? List<ViewPager.OnPageChangeListener> ?: emptyList()
}
