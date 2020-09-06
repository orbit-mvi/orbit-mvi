package com.babylon.orbit2.uitest.plugin.capturestorage.amazon

import com.amazonaws.regions.Regions

internal data class AmazonS3Credentials(
    val bucketName: String,
    val region: Regions,
    val accessKey: String,
    val secretKey: String
)
