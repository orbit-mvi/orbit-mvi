package com.babylon.orbit.launcher.serializer

import android.content.Context
import com.babylon.orbit.launcher.BuildConfig
import com.babylon.orbit.launcher.serializer.repository.SharedPreferencesRepository

internal class SerializerFactory<STATE>(context: Context) {

    val serializer = if (BuildConfig.DEBUG) {
        JsonOrbitSerializer<STATE>(context, SharedPreferencesRepository(context))
    } else {
        NoOpOrbitSerializer<STATE>()
    }
}
