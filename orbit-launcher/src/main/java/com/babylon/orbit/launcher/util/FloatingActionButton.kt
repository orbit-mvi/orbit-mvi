package com.babylon.orbit.launcher.util

import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.babylon.orbit.launcher.R
import com.babylon.orbit.launcher.view.StateMock
import com.babylon.orbit.launcher.screenlist.ui.StatePickerDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

internal fun <STATE : Any> createFAB(
    activity: AppCompatActivity,
    onStateChanged: StatePickerDialogFragment.OnStateChanged,
    mocks: StateMock<STATE>
) =
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )
        .apply {
            gravity = Gravity.BOTTOM or Gravity.END
            val margin = activity.resources.getDimension(R.dimen.fab_margin).toInt()
            setMargins(margin, margin, margin, margin)
        }
        .let {
            FloatingActionButton(activity)
                .apply {
                    layoutParams = it
                    setImageResource(R.drawable.ic_orbit_logo_white_24dp)
                    setOnClickListener { showStatePicker(activity, onStateChanged, mocks) }
                }
        }

private fun <STATE : Any> showStatePicker(
    activity: AppCompatActivity,
    onStateChanged: StatePickerDialogFragment.OnStateChanged,
    mocks: StateMock<STATE>
) {
    val transaction = activity.supportFragmentManager.beginTransaction()

    activity.supportFragmentManager
        .findFragmentByTag(STATE_PICKER_DIALOG_TAG)
        ?.let(transaction::remove)

    transaction.addToBackStack(null)

    StatePickerDialogFragment
        .newInstance(
            mocks.mocks.keys.toList(),
            mocks.additionalMocks.keys.toList()
        ).apply {
            this.onStateChanged = onStateChanged
        }
        .show(transaction, STATE_PICKER_DIALOG_TAG)
}

private const val STATE_PICKER_DIALOG_TAG =
    "com.babylon.orbit.launcher.OrbitActivity_STATE_PICKER_DIALOG_TAG"
