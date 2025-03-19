package org.orbitmvi.orbit.sample.posts.compose.multiplatform

import platform.UIKit.UIDevice

public class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

public actual fun getPlatform(): Platform = IOSPlatform()
