package com.babylon.orbit.launcher.screenlist.business

import com.babylon.orbit.launcher.view.OrbitView

internal data class OrbitScreenState(
    val clazz: Class<out OrbitView<*>>,
    val owner: String
)
