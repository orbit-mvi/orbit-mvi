package com.babylon.orbit2.uitest.engine

import java.io.File

/**
 * @property rootDirectory Device root directory to write files to
 * @property testName Name of the test
 * @property componentName Activity, Fragment or widget name under test
 * @property stateDescription The description of the state
 * @property tags A map of tags associated with a test, for example: app, locale, owner
 */
internal data class UiMetadata(
    val rootDirectory: File,
    val testName: String,
    val componentName: String,
    val stateDescription: String,
    val tags: Map<String, String>
)
