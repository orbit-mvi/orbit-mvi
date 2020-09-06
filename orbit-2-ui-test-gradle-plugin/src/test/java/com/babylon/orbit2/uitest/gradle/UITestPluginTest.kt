package com.babylon.orbit2.uitest.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.kotlin.dsl.apply
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class UITestPluginTest {
    @RegisterExtension
    @JvmField
    val systemProperties = SystemPropertiesExtension()

    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply(UITestPlugin::class)
    }

    @Test
    fun `Task screenshotVerify configured correctly`() {
        project.evaluate()

        systemProperties.apply {
            set(GIT_ORG, "babylonhealth")
            set(GIT_REPO, "babylon-android")
            set(GIT_USERNAME, "user")
            set(GIT_PASSWORD, "password")
            set(HAPPO_API_KEY, "happo_api_key")
            set(HAPPO_SECRET_KEY, "happo_secret_key")
        }

        assertThat(project.tasks.findByName("compareScreenshots"))
            .isInstanceOfSatisfying(CompareScreenshotsTask::class.java) { task ->
                assertThat(task.gitCredentials.orNull).isEqualTo(UsernamePassword("user", "password"))
                assertThat(task.gitHubRepoId.orNull).isEqualTo(GitHubRepositoryId("babylonhealth", "babylon-android"))
                assertThat(task.happoApiKey.orNull).isEqualTo("happo_api_key")
                assertThat(task.happoApiSecret.orNull).isEqualTo("happo_secret_key")
            }
    }
}

private fun Project.evaluate() {
    (this as ProjectInternal).evaluate()
}
