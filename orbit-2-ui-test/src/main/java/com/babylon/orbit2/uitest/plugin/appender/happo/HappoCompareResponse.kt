package com.babylon.orbit2.uitest.plugin.appender.happo

import com.google.gson.annotations.SerializedName

data class HappoCompareResponse(
    @SerializedName("diffs")
    val diffs: List<List<HappoSnapshot>>,

    @SerializedName("ignoredDiffs")
    val ignoredDiffs: List<List<HappoSnapshot>>,

    @SerializedName("added")
    val added: List<HappoSnapshot>,

    @SerializedName("deleted")
    val deleted: List<HappoSnapshot>,

    @SerializedName("unchanged")
    val unchanged: List<HappoSnapshot>,

    @SerializedName("summary")
    val summary: String,

    @SerializedName("equal")
    val equal: Boolean,

    @SerializedName("status")
    val status: HappoResultType,

    @SerializedName("compareUrl")
    val compareUrl: String
)

enum class HappoResultType(val json: String) {
    @SerializedName("success")
    SUCCESS("SUCCESS"),

    @SerializedName("failure")
    FAILURE("FAILURE")
}
