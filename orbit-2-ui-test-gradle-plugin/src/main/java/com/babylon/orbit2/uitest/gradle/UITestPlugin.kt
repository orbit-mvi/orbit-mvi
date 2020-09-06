package com.babylon.orbit2.uitest.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class UITestPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.registerTasks()
    }

    private fun Project.registerTasks() {
        project.tasks.register<CompareScreenshotsTask>("compareScreenshots") {
            getGitCredentials()?.let(gitCredentials::set)
            getGitHubRepoId()?.let(gitHubRepoId::set)
            getEnvOrNull(HAPPO_API_KEY)?.let(happoApiKey::set)
            getEnvOrNull(HAPPO_SECRET_KEY)?.let(happoApiSecret::set)
        }
    }
}

private fun getEnvOrNull(name: String): String? = System.getenv(name) ?: System.getProperty(name)

private fun getGitCredentials(): UsernamePassword? {
    val gitUsername = getEnvOrNull(GIT_USERNAME)
    val gitPassword = getEnvOrNull(GIT_PASSWORD)
    return if (gitUsername != null && gitPassword != null) {
        UsernamePassword(gitUsername, gitPassword)
    } else {
        null
    }
}

private fun getGitHubRepoId(): GitHubRepositoryId? {
    val owner = getEnvOrNull(GIT_ORG)
    val name = getEnvOrNull(GIT_REPO)
    return if (owner != null && name != null) {
        GitHubRepositoryId(owner, name)
    } else {
        null
    }
}

internal const val GIT_USERNAME = "GIT_USERNAME"
internal const val GIT_PASSWORD = "GIT_PASSWORD"
internal const val GIT_ORG = "GIT_ORG"
internal const val GIT_REPO = "GIT_REPO"
internal const val HAPPO_API_KEY = "HAPPO_API_KEY"
internal const val HAPPO_SECRET_KEY = "HAPPO_SECRET_KEY"
