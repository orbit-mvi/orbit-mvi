package com.babylon.orbit2.uitest.engine.util

import java.io.File
import java.io.IOException

internal object PathVerifier {

    fun ensurePathExists(dir: File) {
        val parent = dir.parentFile!!
        if (!parent.exists()) {
            ensurePathExists(parent)
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Unable to create output dir: " + dir.absolutePath)
        }
    }
}
