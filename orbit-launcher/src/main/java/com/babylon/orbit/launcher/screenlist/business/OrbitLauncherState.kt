package com.babylon.orbit.launcher.screenlist.business

import com.babylon.orbit.launcher.view.OrbitView

internal data class OrbitLauncherState(
    val screenState: ScreenState = ScreenState.Loading,
    val views: List<PresentationOrbitView> = emptyList()
)

internal data class PresentationOrbitView(
    val name: String,
    val type: Type,
    val viewClass: Class<out OrbitView<*>>,
    val owner: String
)

enum class Type {
    FRAGMENT,
    ACTIVITY
}

internal enum class ScreenState {
    Loading,
    Ready
}
