package com.babylon.orbit.sample

import android.app.Application
import com.babylon.orbit.sample.di.domainModule
import com.babylon.orbit.sample.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Android context
            androidContext(this@MyApplication)
            // modules
            modules(listOf(domainModule, presentationModule))
        }
    }
}
