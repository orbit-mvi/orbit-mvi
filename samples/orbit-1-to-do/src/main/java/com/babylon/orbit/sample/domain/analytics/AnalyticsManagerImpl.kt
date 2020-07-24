package com.babylon.orbit.sample.domain.analytics

import android.util.Log

class AnalyticsManagerImpl : AnalyticsManager {

    override fun trackAnalytics(key: String) {
        Log.e("analytics", "Log analytics for key $key")
    }
}
