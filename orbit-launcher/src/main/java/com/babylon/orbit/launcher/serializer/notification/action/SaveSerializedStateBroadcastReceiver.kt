package com.babylon.orbit.launcher.serializer.notification.action

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class SaveSerializedStateBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        println("onReceive")
    }

    companion object {

        fun create(context: Context, notificationId: Int): PendingIntent =
            Intent(context, SaveSerializedStateBroadcastReceiver::class.java)
                .apply {
                    action = SAVE
                    putExtra(NOTIFICATION_TAG, notificationId)
                }.let {
                    PendingIntent.getBroadcast(context, 0, it, 0)
                }

        private const val NOTIFICATION_TAG =
            "com.babylon.orbit.launcher.serializer.notification.SaveSerializedStateBroadcastReceiver_NOTIFICATION_TAG"
        private const val SAVE = "com.babylon.orbit.launcher.serializer.notification.SaveSerializedStateBroadcastReceiver_SAVE"
    }
}
