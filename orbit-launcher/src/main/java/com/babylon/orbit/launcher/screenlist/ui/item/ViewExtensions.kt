package com.babylon.orbit.launcher.screenlist.ui.item

import android.view.View

internal fun View.show(isVisible: Boolean = true) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}
