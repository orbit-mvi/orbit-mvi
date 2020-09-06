package com.babylon.orbit2.uitest.engine

import java.io.File
import java.util.Locale

/**
 * Generate file to write a capture to
 */
internal fun UiMetadata.createFile(fileExtension: String) =
    File(rootDirectory, "$testName.$fileExtension")

internal fun UiMetadata.tagsAsString(separator: String) = tags.toSortedMap().values.joinToString(separator) {
    it.toLowerCase(Locale.getDefault()).replace("[^a-zA-Z0-9_]".toRegex(), "_")
}
