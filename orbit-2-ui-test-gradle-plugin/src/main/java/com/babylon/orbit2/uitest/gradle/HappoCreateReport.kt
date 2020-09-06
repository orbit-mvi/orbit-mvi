package com.babylon.orbit2.uitest.gradle

import com.google.gson.annotations.SerializedName

internal data class HappoCreateReport(
    @SerializedName("snaps")
    val snaps: List<HappoSnapshot>,
    @SerializedName("partial")
    val partial: Boolean = true
)
