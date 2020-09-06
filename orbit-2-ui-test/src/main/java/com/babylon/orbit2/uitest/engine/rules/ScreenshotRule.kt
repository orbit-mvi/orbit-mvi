package com.babylon.orbit2.uitest.engine.rules

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.amazonaws.regions.Regions
import com.babylon.orbit2.uitest.engine.SnapshotFactory
import com.babylon.orbit2.uitest.engine.SnapshotRecorder
import com.babylon.orbit2.uitest.plugin.appender.happo.HappoLogger
import com.babylon.orbit2.uitest.plugin.appender.happo.HappoService
import com.babylon.orbit2.uitest.plugin.appender.happo.HappoUiStorageResultAppender
import com.babylon.orbit2.uitest.plugin.captor.interaction.InteractionUiCaptor
import com.babylon.orbit2.uitest.plugin.captor.screenshot.ScreenshotUiCaptor
import com.babylon.orbit2.uitest.plugin.capturestorage.amazon.AmazonS3Credentials
import com.babylon.orbit2.uitest.plugin.capturestorage.amazon.AmazonS3UiCaptureStorage
import com.babylon.orbit2.uitest.plugin.modifier.BackgroundColorModifier
import com.babylon.orbit2.uitest.plugin.modifier.CursorBlinkingDisabler
import com.babylon.orbit2.uitest.plugin.modifier.IdlingResourceRegistrator
import com.babylon.orbit2.uitest.plugin.modifier.resize.ViewResizerFactory
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.reflect.KClass

/**
 * Takes a screenshot after a test completes successfully.
 */
class ScreenshotRule(private val screenUnderTest: KClass<out Any>) : TestWatcher() {
    private val snapshotRecorder = createSnapshotRecorder(InstrumentationRegistry.getArguments())

    override fun succeeded(description: Description) {
        /*val ownedBySquads = description.getAnnotation(OwnedBySquads::class.java)
            ?: description.testClass?.getAnnotation(OwnedBySquads::class.java)
            ?: throw IllegalStateException("OwnedBySquads annotation is missing")
        val screenshotTestAnnotation = description.getAnnotation(ScreenshotTest::class.java)
            ?: throw IllegalStateException("ScreenshotTest annotation is missing")*/

        snapshotRecorder.recordScreenShot(
            activity = getCurrentActivity(),
            screenUnderTest = screenUnderTest,
            testName = "${description.className}#${description.methodName}",
            stateDescription = ""//screenshotTestAnnotation.description,
            //tags = mapOf("owner" to ownedBySquads.squadNames)
        )
    }

    private fun getCurrentActivity(): Activity {
        var result: Collection<Activity> = emptyList()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        instrumentation.runOnMainSync {
            result = ActivityLifecycleMonitorRegistry
                .getInstance()
                .getActivitiesInStage(Stage.RESUMED)
        }

        return result.first()
    }

    private fun createSnapshotRecorder(arguments: Bundle): SnapshotRecorder {
        val happoApiKey = arguments.getString(ARG_HAPPO_API_KEY)
        val happoSecretKey = arguments.getString(ARG_HAPPO_SECRET_KEY)
        val happoBranchSha = arguments.getString(ARG_HAPPO_BRANCH_SHA)
        val happoAppender = if (happoApiKey != null && happoSecretKey != null && happoBranchSha != null) {
            HappoUiStorageResultAppender(
                HappoService(
                    branchSha = happoBranchSha,
                    apiKey = happoApiKey,
                    apiSecret = happoSecretKey,
                    happoLogger = object : HappoLogger {
                        override fun log(message: String) {
                            Log.d(LOG_TAG, message)
                        }
                    })
            )
        } else {
            Log.w(
                LOG_TAG,
                "Happo report will not be created because instrumentation arguments $ARG_HAPPO_API_KEY, $ARG_HAPPO_SECRET_KEY" +
                        " or $ARG_HAPPO_BRANCH_SHA are missing."
            )
            null
        }

        val screenshotStorageAccessKey = arguments.getString(ARG_STORAGE_ACCESS_KEY)
        val screenshotStorageSecretKey = arguments.getString(ARG_STORAGE_SECRET_KEY)
        val captureStorage = if (screenshotStorageAccessKey != null && screenshotStorageSecretKey != null) {
            AmazonS3UiCaptureStorage(
                AmazonS3Credentials(
                    bucketName = "babylon-dev-uk-android-screenshot-internal",
                    region = Regions.EU_WEST_2,
                    accessKey = screenshotStorageAccessKey,
                    secretKey = screenshotStorageSecretKey
                )
            )
        } else {
            Log.w(
                LOG_TAG, "Screenshots will not be uploaded because instrumentation arguments " +
                        "$ARG_STORAGE_ACCESS_KEY or $ARG_STORAGE_SECRET_KEY are missing."
            )
            null
        }

        return SnapshotRecorder(
            snapshotFactory = SnapshotFactory(
                captors = listOf(ScreenshotUiCaptor(), InteractionUiCaptor()),
                captureStorage = captureStorage,
                appenders = listOfNotNull(happoAppender),
                viewModifiers = listOf(
                    BackgroundColorModifier(),
                    IdlingResourceRegistrator(),
                    ViewResizerFactory.createViewResizer(),
                    CursorBlinkingDisabler()
                )
            )
        )
    }

    companion object {
        private const val LOG_TAG = "Screenshot"
        private const val ARG_HAPPO_API_KEY = "happoApiKey"
        private const val ARG_HAPPO_SECRET_KEY = "happoSecretKey"
        private const val ARG_HAPPO_BRANCH_SHA = "happoBranchSha"
        private const val ARG_STORAGE_ACCESS_KEY = "screenshotStorageAccessKey"
        private const val ARG_STORAGE_SECRET_KEY = "screenshotStorageSecretKey"
    }
}
