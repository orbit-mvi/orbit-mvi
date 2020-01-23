package com.babylon.orbit.launcher.screenlist.business

import android.content.Context
import com.babylon.orbit.launcher.view.OrbitActivity
import com.babylon.orbit.launcher.screenlist.ui.OrbitWrapperActivity
import com.babylon.orbit.launcher.screenlist.business.Type.FRAGMENT
import com.babylon.orbit.launcher.screenlist.business.Type.ACTIVITY

internal class OrbitLauncherSideEffect(
    private val context: Context
) {

    fun navigateTo(event: Any?) {
        if (event is OrbitViewAction.Selected) {
            when (event.orbitView.type) {
                FRAGMENT -> OrbitWrapperActivity.start(context, event.orbitView.viewClass)
                ACTIVITY -> OrbitActivity.startAsMockConsumer(context, event.orbitView.viewClass)
            }
        }
    }
}
