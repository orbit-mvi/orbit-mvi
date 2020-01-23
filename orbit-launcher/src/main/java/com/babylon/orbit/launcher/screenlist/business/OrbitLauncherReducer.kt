package com.babylon.orbit.launcher.screenlist.business

import com.babylon.orbit.launcher.screenlist.business.Type.ACTIVITY
import com.babylon.orbit.launcher.screenlist.business.Type.FRAGMENT
import com.babylon.orbit.launcher.view.OrbitActivity
import com.babylon.orbit.launcher.view.OrbitFragment
import com.babylon.orbit.launcher.view.OrbitView

internal class OrbitLauncherReducer {

    fun reduceViews(
        currentState: OrbitLauncherState,
        event: List<OrbitScreenState>
    ): OrbitLauncherState =
        currentState.copy(
            screenState = ScreenState.Ready,
            views = event
                .map { screenState ->
                    PresentationOrbitView(
                        name = screenState.clazz.simpleName,
                        type = when {
                            isOrbit(screenState.clazz, OrbitActivity::class.java) -> ACTIVITY
                            isOrbit(screenState.clazz, OrbitFragment::class.java) -> FRAGMENT
                            else -> throw IllegalStateException("Unsupported Orbit screen type.")
                        },
                        viewClass = screenState.clazz,
                        owner = screenState.owner
                    )
                }
        )

    private fun isOrbit(
        viewClazz: Class<out OrbitView<*>>,
        typeClazz: Class<*>
    ): Boolean {
        var clazz: Class<*>? = viewClazz
        while (clazz != null) {
            clazz = clazz.superclass
            if (clazz == typeClazz) return true
        }

        return false
    }
}
