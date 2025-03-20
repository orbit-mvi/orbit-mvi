package org.orbitmvi.orbit.sample.posts.compose.multiplatform

public interface Platform {
    public val name: String
}

public expect fun getPlatform(): Platform
