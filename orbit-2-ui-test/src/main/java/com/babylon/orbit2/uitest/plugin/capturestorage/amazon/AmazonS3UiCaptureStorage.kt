package com.babylon.orbit2.uitest.plugin.capturestorage.amazon

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.babylon.orbit2.uitest.plugin.captor.UiCaptureResult
import com.babylon.orbit2.uitest.plugin.capturestorage.UiCaptureStorage
import com.babylon.orbit2.uitest.plugin.capturestorage.UiStorageResult

internal class AmazonS3UiCaptureStorage(
    private val arguments: AmazonS3Credentials
) : UiCaptureStorage {

    private val amazonClient by lazy {
        val credentials = BasicAWSCredentials(arguments.accessKey, arguments.secretKey)

        val region = Region.getRegion(arguments.region)
        AmazonS3Client(credentials, region)
    }

    override fun store(captureResult: UiCaptureResult): UiStorageResult {
        val uploadName = "${captureResult.hash}.${captureResult.file.extension}"

        if (!amazonClient.doesObjectExist(arguments.bucketName, uploadName)) {
            val request = PutObjectRequest(arguments.bucketName, uploadName, captureResult.file)
            amazonClient.putObject(request)
        }

        val url = "https://${arguments.bucketName}.s3.${arguments.region.getName()}.amazonaws.com/$uploadName"
        return UiStorageResult(captureResult, url)
    }
}
