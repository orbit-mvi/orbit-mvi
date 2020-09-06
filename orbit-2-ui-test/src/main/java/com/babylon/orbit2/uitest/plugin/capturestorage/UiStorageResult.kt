package com.babylon.orbit2.uitest.plugin.capturestorage

import com.babylon.orbit2.uitest.plugin.captor.UiCaptureResult

internal data class UiStorageResult(
    val captureResult: UiCaptureResult,
    val url: String
)
