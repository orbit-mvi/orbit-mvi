package com.babylon.orbit.launcher.serializer.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.babylon.orbit.launcher.R
import com.babylon.orbit.launcher.serializer.notification.action.ClearSerializedStateBroadcastReceiver
import com.babylon.orbit.launcher.serializer.notification.action.SaveSerializedStateBroadcastReceiver
import com.babylon.orbit.launcher.view.OrbitView

internal class Notification<STATE>(
    private val context: Context,
    private val clazz: Class<out OrbitView<STATE>>
) {

    private val notificationManager by lazy { NotificationManagerCompat.from(context) }

    private val notification by lazy {
        val deleteIntent = ClearSerializedStateBroadcastReceiver.create(context, NOTIFICATION_ID, clazz.name)
        val saveIntent = SaveSerializedStateBroadcastReceiver.create(context, NOTIFICATION_ID)

        NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_orbit_logo_white_24dp)
            .setContentTitle("State Recorder")
            .setContentText("Recording ${clazz.simpleName} state")
            .setOngoing(true)
            .addAction(R.drawable.ic_save_black_24dp, context.getString(R.string.save), saveIntent)
            .addAction(R.drawable.ic_delete_black_24dp, context.getString(R.string.clear), deleteIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun show() {
        createNotificationChannel()

        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    fun hide() = notificationManager.cancel(NOTIFICATION_ID)

    companion object {

        private const val CHANNEL_ID = "SharePreferencesOrbitSerializer_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1337
    }
}
