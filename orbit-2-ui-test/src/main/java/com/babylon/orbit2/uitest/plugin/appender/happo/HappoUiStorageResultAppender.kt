package com.babylon.orbit2.uitest.plugin.appender.happo

import com.babylon.orbit2.uitest.engine.UiMetadata
import com.babylon.orbit2.uitest.engine.tagsAsString
import com.babylon.orbit2.uitest.plugin.appender.UiStorageResultAppender
import com.babylon.orbit2.uitest.plugin.captor.UiCaptureResult
import com.babylon.orbit2.uitest.plugin.capturestorage.UiStorageResult

internal class HappoUiStorageResultAppender(
    private val happoService: HappoService
) : UiStorageResultAppender {

    override fun append(storageResult: UiStorageResult, metadata: UiMetadata) {
        val happoSnapshot = createSnapshot(storageResult, metadata)
        happoService.appendToReport(happoSnapshot)
    }

    private fun createSnapshot(storageResult: UiStorageResult, metadata: UiMetadata) = HappoSnapshot(
        url = storageResult.url,
        variant = metadata.stateDescription,
        component = metadata.componentName,
        target = "${storageResult.captureResult.captorName}; ${metadata.tagsAsString("; ")}",
        width = (storageResult.captureResult as? UiCaptureResult.Image)?.width ?: 1,
        height = (storageResult.captureResult as? UiCaptureResult.Image)?.height ?: 1
    )
}
