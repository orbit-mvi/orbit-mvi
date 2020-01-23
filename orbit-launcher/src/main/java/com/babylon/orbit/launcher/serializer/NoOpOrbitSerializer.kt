package com.babylon.orbit.launcher.serializer

import com.babylon.orbit.launcher.view.OrbitView

internal class NoOpOrbitSerializer<STATE> : OrbitStateSerializer<STATE> {

    override fun serialize(viewClass: Class<out OrbitView<STATE>>, state: STATE) = Unit

    override fun deserialize(viewClass: Class<out OrbitView<STATE>>, stateClass: Class<out STATE>): List<Pair<String, STATE>> = emptyList()

    override fun cleanup() = Unit
}
