package com.babylon.orbit2.uitest.gradle

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class SystemPropertiesExtension : AfterEachCallback {
    private val modifiedProperties = mutableSetOf<String>()

    fun set(key: String, value: String) {
        modifiedProperties += key
        System.setProperty(key, value)
    }

    override fun afterEach(context: ExtensionContext) {
        modifiedProperties.forEach { System.clearProperty(it) }
    }
}
