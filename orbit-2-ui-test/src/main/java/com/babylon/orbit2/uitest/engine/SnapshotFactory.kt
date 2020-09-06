package com.babylon.orbit2.uitest.engine

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.test.espresso.Espresso
import com.babylon.orbit2.uitest.engine.util.PathVerifier
import com.babylon.orbit2.uitest.plugin.appender.UiStorageResultAppender
import com.babylon.orbit2.uitest.plugin.captor.UiCaptor
import com.babylon.orbit2.uitest.plugin.capturestorage.UiCaptureStorage
import com.babylon.orbit2.uitest.plugin.modifier.ViewModifier
import kotlin.reflect.KClass

internal class SnapshotFactory(
    private val captors: List<UiCaptor>,
    private val captureStorage: UiCaptureStorage?,
    private val appenders: List<UiStorageResultAppender>,
    private val viewModifiers: List<ViewModifier>
) {

    fun createSnapshot(activity: Activity, screenUnderTest: KClass<out Any>, metadata: UiMetadata) {

        PathVerifier.ensurePathExists(metadata.rootDirectory)

        // Load screen in the given state
        val rootView = findRootView(activity)

        viewModifiers.forEach { it.modify(rootView) }

        Espresso.onIdle()

        val captures = captors.mapNotNull { captor ->
            captor.capture(screenUnderTest, rootView, metadata)
        }

        // Upload step
        val storageResults = captureStorage?.let {
            captures.map { capture ->
                captureStorage.store(capture)
            }
        }

        // Add to report
        storageResults?.forEach { storageResult ->
            appenders.forEach {
                it.append(storageResult, metadata)
            }
        }
    }

    private fun findRootView(activity: Activity): View {
        return if (!activity.window.decorView.hasFocus()) {
            (activity as? AppCompatActivity)?.supportFragmentManager?.fragments?.firstOrNull {
                it.isVisible && it is DialogFragment
            }?.view ?: throw IllegalStateException("No visible fragments")
        } else {
            activity.window.decorView.findViewById<View>(android.R.id.content)
        }
    }
}
