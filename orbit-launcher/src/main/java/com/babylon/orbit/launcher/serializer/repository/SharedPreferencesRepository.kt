package com.babylon.orbit.launcher.serializer.repository

import android.content.Context
import com.babylon.orbit.launcher.view.OrbitView

internal class SharedPreferencesRepository<STATE>(
    context: Context
) : Repository<STATE> {

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun write(key: Class<out OrbitView<STATE>>, value: String) {
        getStates(key)
            .plus(value)
            .run {
                sharedPreferences
                    .edit()
                    .putStringSet(key.name, this)
                    .commit()
            }
    }

    override fun read(key: Class<out OrbitView<STATE>>): List<String> = getStates(key).toList()

    private fun getStates(key: Class<out OrbitView<STATE>>) = sharedPreferences.getStringSet(key.name, emptySet()) ?: emptySet()

    override fun clear(key: Class<out OrbitView<STATE>>) = sharedPreferences.edit().remove(key.name).apply()

    private companion object {

        const val SHARED_PREFS_NAME = "com.babylon.orbit.launcher"
    }
}
