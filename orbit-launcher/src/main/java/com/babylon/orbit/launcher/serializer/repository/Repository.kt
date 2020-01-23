package com.babylon.orbit.launcher.serializer.repository

import com.babylon.orbit.launcher.view.OrbitView

internal interface Repository<STATE> {

    fun write(key: Class<out OrbitView<STATE>>, value: String)

    fun read(key: Class<out OrbitView<STATE>>): List<String>

    fun clear(key: Class<out OrbitView<STATE>>)
}
