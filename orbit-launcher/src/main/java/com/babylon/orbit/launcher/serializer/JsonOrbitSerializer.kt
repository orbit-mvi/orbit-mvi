package com.babylon.orbit.launcher.serializer

import android.content.Context
import com.babylon.orbit.launcher.serializer.notification.Notification
import com.babylon.orbit.launcher.serializer.repository.Repository
import com.babylon.orbit.launcher.view.OrbitView
import java.sql.Timestamp

internal class JsonOrbitSerializer<STATE>(
    private val context: Context,
    private val repository: Repository<STATE>
) : OrbitStateSerializer<STATE> {

    private val json by lazy { Json }

    private var notification: Notification<STATE>? = null

    override fun serialize(
        viewClass: Class<out OrbitView<STATE>>,
        state: STATE
    ) = repository
        .write(viewClass, json.toJson(state.wrapWithTimeStamp()))
        .also { showNotification(viewClass) }

    private fun showNotification(viewClass: Class<out OrbitView<STATE>>) {
        if (notification == null) {
            notification = Notification(context, viewClass)
            notification?.show()
        }
    }

    override fun deserialize(
        viewClass: Class<out OrbitView<STATE>>,
        stateClass: Class<out STATE>
    ): List<Pair<String, STATE>> =
        repository
            .read(viewClass)
            .map { state ->
                unwrapState(state)
                    .let { (jsonState, timestamp) ->
                        TimestampWrapper(
                            timestamp,
                            json.fromJson(jsonState, stateClass)
                        )
                    }
            }
            .sortedBy(TimestampWrapper<STATE>::timestamp)
            .map { Timestamp(it.timestamp).toString() to it.state }

    private fun unwrapState(jsonState: String) = jsonState
        .removePrefix("{\"state\":")
        .removeSuffix("}")
        .split(",\"timestamp\":")
        .let {
            it[0] to it[1].toLong()
        }

    override fun cleanup() {
        notification?.hide()
        notification = null
    }

    private data class TimestampWrapper<STATE>(val timestamp: Long, val state: STATE)

    private fun <STATE> STATE.wrapWithTimeStamp() = TimestampWrapper(System.currentTimeMillis(), this)
}
