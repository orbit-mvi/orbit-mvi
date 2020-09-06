package com.babylon.orbit2.uitest.plugin.captor

import okio.ByteString
import java.io.File

internal sealed class UiCaptureResult {
    abstract val captorName: String
    abstract val file: File

    val hash: String by lazy {
        ByteString.of(*file.readBytes()).sha256().hex()
    }

    data class Image(
        override val captorName: String,
        override val file: File,
        val height: Int,
        val width: Int
    ) : UiCaptureResult()

    data class Json(
        override val captorName: String,
        override val file: File
    ) : UiCaptureResult()
}
