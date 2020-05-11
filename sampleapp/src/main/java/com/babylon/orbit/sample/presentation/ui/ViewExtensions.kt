package com.babylon.orbit.sample.presentation.ui

import android.view.View

fun View.show(isVisible: Boolean = true) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}
