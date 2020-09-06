package com.babylon.orbit2.uitest.plugin.appender

import com.babylon.orbit2.uitest.engine.UiMetadata
import com.babylon.orbit2.uitest.plugin.capturestorage.UiStorageResult

internal interface UiStorageResultAppender {
    fun append(storageResult: UiStorageResult, metadata: UiMetadata)
}
