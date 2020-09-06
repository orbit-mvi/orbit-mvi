package com.babylon.orbit2.uitest.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class CompareScreenshotsTask : DefaultTask() {

    @get:Internal
    abstract val gitCredentials: Property<UsernamePassword>

    @get:Input
    abstract val gitHubRepoId: Property<GitHubRepositoryId>

    @get:Internal
    abstract val happoApiKey: Property<String>

    @get:Internal
    abstract val happoApiSecret: Property<String>

    @get:Input
    @get:Option(option = "before", description = "Commit hash of \"before\" report")
    abstract val beforeSha: Property<String>

    @get:Input
    @get:Option(option = "after", description = "Commit hash of \"after\" report")
    abstract val afterSha: Property<String>

    @get:Input
    @get:Option(option = "pr", description = "ID of GitHub pull request")
    abstract val pullRequestId: Property<String>

    init {
        description = "Compares screenshots from a pull request with the base branch and generates a Happo report."
        group = "screenshot"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun compareScreenshots() {
        ComparisonScreenshotAction(
            gitCredentials = gitCredentials.get(),
            gitHubRepositoryId = gitHubRepoId.get(),
            happoApiKey = happoApiKey.get(),
            happoApiSecret = happoApiSecret.get(),
            logger = logger
        ).verifyScreenshots(
            beforeSha = beforeSha.get(),
            afterSha = afterSha.get(),
            pullRequestId = pullRequestId.get().toInt()
        )
    }
}
