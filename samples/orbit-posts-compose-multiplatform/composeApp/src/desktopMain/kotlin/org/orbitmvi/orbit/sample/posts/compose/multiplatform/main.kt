@file:Suppress("Filename")

package org.orbitmvi.orbit.sample.posts.compose.multiplatform

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

public fun main(): Unit = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "orbit-posts-compose-multiplatform",
    ) {
        App()
    }
}
