package com.babylon.orbit.launcher.serializer.notification.action

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class ClearSerializedStateBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context
            .getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(intent.getStringExtra(CLEAR_KEY))
            .apply()
    }

    companion object {

        fun create(
            context: Context,
            notificationId: Int,
            key: String
        ): PendingIntent =
            Intent(context, ClearSerializedStateBroadcastReceiver::class.java)
                .apply {
                    action = CLEAR
                    putExtra(NOTIFICATION_TAG, notificationId)
                    putExtra(CLEAR_KEY, key)
                }.let {
                    PendingIntent.getBroadcast(context, 0, it, 0)
                }

        private const val NOTIFICATION_TAG =
            "com.babylon.orbit.launcher.serializer.notification.ClearSerializedStateBroadcastReceiver_NOTIFICATION_TAG"
        private const val CLEAR =
            "com.babylon.orbit.launcher.serializer.notification.ClearSerializedStateBroadcastReceiver_CLEAR"
        private const val CLEAR_KEY =
            "com.babylon.orbit.launcher.serializer.notification.ClearSerializedStateBroadcastReceiver_CLEAR_KEY"

        private const val SHARED_PREFS_NAME = "com.babylon.orbit.launcher"
    }
}
