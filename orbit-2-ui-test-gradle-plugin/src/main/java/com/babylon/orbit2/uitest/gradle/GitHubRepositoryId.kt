package com.babylon.orbit2.uitest.gradle

import java.io.Serializable

data class GitHubRepositoryId(
    val owner: String,
    val name: String
) : Serializable
