package org.orbitmvi.orbit.sample.posts.compose.multiplatform

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

public fun mainViewController(): UIViewController = ComposeUIViewController { App() }
