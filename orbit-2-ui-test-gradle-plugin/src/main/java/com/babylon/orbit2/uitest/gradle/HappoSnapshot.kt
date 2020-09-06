package com.babylon.orbit2.uitest.gradle

import com.google.gson.annotations.SerializedName

data class HappoSnapshot(
    @SerializedName("url")
    val url: String,
    @SerializedName("variant")
    val variant: String,
    @SerializedName("target")
    val target: String,
    @SerializedName("component")
    val component: String,
    @SerializedName("height")
    val height: Int,
    @SerializedName("width")
    val width: Int
)
