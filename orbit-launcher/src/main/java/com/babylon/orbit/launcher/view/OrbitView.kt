package com.babylon.orbit.launcher.view

internal interface OrbitView<State> {

    val owner: String

    fun render(state: State)
}
