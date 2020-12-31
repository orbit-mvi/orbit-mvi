/*
 * Copyright 2021 Mikolaj Leszczynski & Matthew Dolan
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.orbitmvi.orbit.sample.posts.app.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import org.orbitmvi.orbit.sample.posts.R
import com.google.android.material.appbar.AppBarLayout

/**
 * Ensures Toolbar is not elevated when a recyclerview or nestedscrollview is scrolled to the top
 */
@Suppress("unused")
class ShadowScrollBehavior(context: Context, attrs: AttributeSet) : AppBarLayout.ScrollingViewBehavior(context, attrs) {

    @SuppressLint("PrivateResource")
    private val maxElevation = context.resources.getDimensionPixelSize(R.dimen.design_appbar_elevation).toFloat()

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View):
            Boolean {
        if (dependency is AppBarLayout) {
            when (child) {
                is NestedScrollView -> {
                    setElevation(child, dependency)
                    addScrollListener(child, dependency)
                }
                is RecyclerView -> {
                    setElevation(child, dependency)
                    addScrollListener(child, dependency)
                }
            }
        }

        return super.onDependentViewChanged(parent, child, dependency)
    }

    private fun addScrollListener(child: NestedScrollView, dependency: AppBarLayout) {
        child.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
            setElevation(child, dependency)
        }
    }

    private fun addScrollListener(child: RecyclerView, dependency: AppBarLayout) {
        child.clearOnScrollListeners()
        child.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    setElevation(recyclerView, dependency)
                }
            }
        )
    }

    private fun setElevation(view: View, appBarLayout: AppBarLayout) {
        val elevation = if (view.canScrollVertically(SCROLL_DIRECTION_UP)) maxElevation else 0f

        ViewCompat.setElevation(appBarLayout, elevation)
    }

    companion object {
        private const val SCROLL_DIRECTION_UP = -1
    }
}
