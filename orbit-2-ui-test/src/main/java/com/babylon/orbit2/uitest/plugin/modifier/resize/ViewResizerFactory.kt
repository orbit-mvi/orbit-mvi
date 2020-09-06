package com.babylon.orbit2.uitest.plugin.modifier.resize

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.babylon.orbit2.uitest.plugin.modifier.resize.ScrollingViewFinder
import com.babylon.orbit2.uitest.plugin.modifier.resize.ViewResizer

internal object ViewResizerFactory {

    fun createViewResizer(): ViewResizer<RecyclerView> =
        ViewResizer(
            scrollingViewFinder = object : ScrollingViewFinder<RecyclerView>() {
                override fun isScrollingView(viewGroup: ViewGroup) = viewGroup is RecyclerView
            }
        )
}
