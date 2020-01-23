package com.babylon.orbit.launcher.screenlist.business

import com.babylon.orbit.LifecycleAction
import com.babylon.orbit.OrbitViewModel

internal class OrbitLauncherViewModel(
    private val transformers: OrbitLauncherTransformer,
    private val reducers: OrbitLauncherReducer,
    private val sideEffects: OrbitLauncherSideEffect
) : OrbitViewModel<OrbitLauncherState, Unit>(
    OrbitLauncherState(), {

        perform("load Orbit screens")
            .on(LifecycleAction.Created::class.java)
            .transform { transformers.loadScreens(eventObservable) }
            .reduce { reducers.reduceViews(currentState, event) }

        perform("click on Orbit screen")
            .on(OrbitViewAction.Selected::class.java)
            .sideEffect { sideEffects.navigateTo(event) }
    })
