package com.babylon.orbit2.uitest.plugin.modifier.resize

import android.view.ViewGroup
import androidx.core.view.children

internal abstract class ScrollingViewFinder<T : ViewGroup> {

    @Suppress("UNCHECKED_CAST")
    fun findScrollingView(viewGroup: ViewGroup): T? {
        viewGroup.children.filterIsInstance<ViewGroup>().forEach { childView ->
            if (isScrollingView(childView)) {
                return childView as? T
            } else {
                findScrollingView(childView)?.let {
                    return it
                }
            }
        }

        return null
    }

    abstract fun isScrollingView(viewGroup: ViewGroup): Boolean
}
