package com.babylon.orbit2.uitest.plugin.appender.happo

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface HappoApi {

    @POST("reports/{sha}")
    fun createReport(
        @Path("sha") sha: String,
        @Body body: HappoCreateReport
    ): Call<HappoResponseStatus>

    @POST("reports/{beforeSha}/compare/{afterSha}")
    fun compareReports(
        @Path("beforeSha") beforeSha: String,
        @Path("afterSha") afterSha: String,
        @Body body: HappoCompareRequestBody
    ): Call<HappoCompareResponse>
}
