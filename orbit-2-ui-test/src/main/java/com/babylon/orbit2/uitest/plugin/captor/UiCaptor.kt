package com.babylon.orbit2.uitest.plugin.captor

import android.view.View
import com.babylon.orbit2.uitest.engine.UiMetadata
import kotlin.reflect.KClass

internal interface UiCaptor {
    fun capture(screenUnderTest: KClass<out Any>, rootView: View, metadata: UiMetadata): UiCaptureResult?
}
