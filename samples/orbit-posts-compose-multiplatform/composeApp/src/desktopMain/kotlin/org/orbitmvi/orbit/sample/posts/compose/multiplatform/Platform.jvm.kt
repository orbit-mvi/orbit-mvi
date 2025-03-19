package org.orbitmvi.orbit.sample.posts.compose.multiplatform

public class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

public actual fun getPlatform(): Platform = JVMPlatform()
