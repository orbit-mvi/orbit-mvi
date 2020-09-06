package com.babylon.orbit2.uitest.gradle

import org.eclipse.egit.github.core.RepositoryId
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.IssueService
import org.gradle.api.logging.Logger

internal class ComparisonScreenshotAction(
    private val gitCredentials: UsernamePassword,
    private val gitHubRepositoryId: GitHubRepositoryId,
    private val happoApiKey: String,
    private val happoApiSecret: String,
    private val logger: Logger
) {

    fun verifyScreenshots(beforeSha: String, afterSha: String, pullRequestId: Int) {
        val gitHubClient = GitHubClient()

        val client = gitHubClient.setCredentials(gitCredentials.username, gitCredentials.password)
        val repo = RepositoryId(gitHubRepositoryId.owner, gitHubRepositoryId.name)

        val issueService = IssueService(client)
        val happoService = createHappoService(afterSha)
        val pullRequestUrl = "https://github.com/${gitHubRepositoryId.owner}/${gitHubRepositoryId.name}/pull/$pullRequestId"

        try {
            val response = happoService.compareReports(beforeSha, afterSha, pullRequestUrl)
            // Fix this somehow... The report URL from develop doesn't exist so Happo is returning a 402
            logger.lifecycle(
                "The report comparison had a status code of ${response.status.json}, the report link" +
                        " can be found on ${response.compareUrl}"
            )
            postComment(response.summary, issueService, repo, pullRequestId)
        } catch (error: HappoReportNotFound) {
            logger.lifecycle("report not found, ignoring this verification with message ${error.message}")
            postComment(error.message ?: "", issueService, repo, pullRequestId)
        }
    }

    private fun postComment(content: String, issueService: IssueService, repositoryId: RepositoryId, pullRequestId: Int) {
        deletePreviousComment(issueService, repositoryId, pullRequestId)
        issueService.createComment(repositoryId, pullRequestId, HAPPO_REPORT_COMMENT_PREFIX + content)
    }

    private fun deletePreviousComment(issueService: IssueService, repositoryId: RepositoryId, pullRequestId: Int) {
        val comments = issueService.getComments(repositoryId, pullRequestId)
        comments.forEach { comment ->
            // Only delete comments that start with the happo prefix
            if (comment.body.startsWith(HAPPO_REPORT_COMMENT_PREFIX)) {
                issueService.deleteComment(repositoryId, comment.id)
            }
        }
    }

    private fun createHappoService(pullRequestSha: String) = HappoService(
        branchSha = pullRequestSha,
        apiKey = happoApiKey,
        apiSecret = happoApiSecret,
        happoLogger = object : HappoLogger {
            override fun log(message: String) {
                logger.lifecycle(message)
            }
        }
    )

    private companion object {
        const val HAPPO_REPORT_COMMENT_PREFIX = "Happo report summary: \n"
    }
}
