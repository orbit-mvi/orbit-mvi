package org.orbitmvi.orbit.sample.posts.compose.multiplatform

public class Greeting {
    private val platform = getPlatform()

    public fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}
