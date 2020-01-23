package com.babylon.orbit.launcher.screenlist.business

internal sealed class OrbitViewAction {

    data class Selected(val orbitView: PresentationOrbitView) : OrbitViewAction()
}
