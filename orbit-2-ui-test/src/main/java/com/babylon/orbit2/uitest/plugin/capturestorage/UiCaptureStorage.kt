package com.babylon.orbit2.uitest.plugin.capturestorage

import com.babylon.orbit2.uitest.plugin.captor.UiCaptureResult

internal interface UiCaptureStorage {
    fun store(captureResult: UiCaptureResult): UiStorageResult
}
