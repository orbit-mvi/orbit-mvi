package com.babylon.orbit2.uitest.engine

import android.app.Activity
import java.io.File
import java.util.Locale
import kotlin.reflect.KClass

internal class SnapshotRecorder(
    private val snapshotFactory: SnapshotFactory
) {

    fun recordScreenShot(
        activity: Activity,
        screenUnderTest: KClass<out Any>,
        testName: String,
        stateDescription: String,
        tags: Map<String, String> = emptyMap()
    ) {
        val screenshotDir = File(activity.filesDir, "screenshots")
        val metadata = UiMetadata(
            rootDirectory = screenshotDir,
            testName = testName,
            componentName = screenUnderTest.java.name,
            stateDescription = stateDescription,
            tags = tags + mapOf(
                "app" to activity.applicationInfo.loadLabel(activity.packageManager).toString(),
                "locale" to Locale.getDefault().toString()
            )
        )

        snapshotFactory.createSnapshot(activity, screenUnderTest, metadata)
    }
}
