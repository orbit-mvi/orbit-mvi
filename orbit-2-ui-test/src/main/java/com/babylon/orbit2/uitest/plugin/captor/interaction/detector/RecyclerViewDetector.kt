package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction
import com.babylon.orbit2.uitest.plugin.captor.interaction.description

internal class RecyclerViewDetector : InteractionDetector {

    override fun detect(view: View): Sequence<PendingInteraction> {
        if (view is RecyclerView) {
            return view.scrollListeners.asSequence().flatMap { listener ->
                sequenceOf(
                    PendingInteraction(
                        source = view.description,
                        event = "scroll state changed",
                        executor = {
                            listener.onScrollStateChanged(view, SCROLL_STATE_IDLE)
                        }
                    ),
                    PendingInteraction(
                        source = view.description,
                        event = "scrolled",
                        executor = {
                            listener.onScrolled(view, 0, 0)
                        }
                    )
                )
            }
        }

        return emptySequence()
    }

    private val RecyclerView.scrollListeners: List<RecyclerView.OnScrollListener>
        @Suppress("UNCHECKED_CAST")
        get() = RecyclerView::class.java.getDeclaredField("mScrollListeners").apply {
            isAccessible = true
        }.get(this) as? List<RecyclerView.OnScrollListener> ?: emptyList()
}
