package com.babylon.orbit2.uitest.gradle

import com.google.gson.annotations.SerializedName

internal data class HappoResponseStatus(
    @SerializedName("url")
    val url: String
)
