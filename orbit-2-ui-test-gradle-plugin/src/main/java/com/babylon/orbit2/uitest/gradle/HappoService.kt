package com.babylon.orbit2.uitest.gradle

import com.google.gson.Gson
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.IOException
import java.util.concurrent.TimeUnit

class HappoService(
    private val branchSha: String,
    private val apiKey: String,
    private val apiSecret: String,
    private val happoLogger: HappoLogger
) {

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(BasicAuthInterceptor(apiKey, apiSecret))
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .callTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://happo.io/api/")
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(okHttpClient)
            .validateEagerly(true)
            .build()
    }

    private val happoApi by lazy {
        retrofit.create<HappoApi>()
    }

    fun appendToReport(snapshot: HappoSnapshot) {
        val happoReport = HappoCreateReport(
            snaps = listOf(snapshot)
        )

        happoLogger.log("Appending test to report with name ${snapshot.component}")
        val response = retryWithExponentialBackoff { happoApi.createReport(branchSha, happoReport).executeCall() }
        happoLogger.log("The Happo report url is ${response.url}")
    }

    fun compareReports(beforeSha: String, afterSha: String, pullRequestUrl: String): HappoCompareResponse {
        happoLogger.log("Comparing reports with beforeSha $beforeSha and afterSha $afterSha")
        val requestBody = HappoCompareRequestBody(pullRequestUrl)

        return retryWithExponentialBackoff(setOf(HappoReportNotFound::class)) {
            happoApi.compareReports(beforeSha, afterSha, requestBody).executeCall { errorMessage ->
                if (errorMessage.contains(NO_REPORT_EXISTS_ERROR_MSG)) {
                    HappoReportNotFound(errorMessage)
                } else {
                    IOException(errorMessage)
                }
            }
        }
    }

    companion object {
        private const val NO_REPORT_EXISTS_ERROR_MSG = "No report exists for"
        private const val TIMEOUT = 15L
    }
}

private fun <T> Call<T>.executeCall(exceptionMapper: (message: String) -> Throwable = { IOException(it) }): T {
    val response = execute()

    if (response.isSuccessful) {
        return response.body()!!
    } else {
        val errorMessage = response.errorBody()!!.string()
        throw exceptionMapper(errorMessage)
    }
}

private class BasicAuthInterceptor(private val apiKey: String, private val apiSecret: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val token = Credentials.basic(apiKey, apiSecret)
        val authenticatedRequest = request.newBuilder()
            .header("Authorization", token).build()
        return chain.proceed(authenticatedRequest)
    }
}
