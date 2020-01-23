package com.babylon.orbit.launcher.serializer

import com.babylon.orbit.launcher.view.OrbitView

internal interface OrbitStateSerializer<STATE> {

    fun serialize(viewClass: Class<out OrbitView<STATE>>, state: STATE)

    fun deserialize(viewClass: Class<out OrbitView<STATE>>, stateClass: Class<out STATE>): List<Pair<String, STATE>>

    fun cleanup()
}
