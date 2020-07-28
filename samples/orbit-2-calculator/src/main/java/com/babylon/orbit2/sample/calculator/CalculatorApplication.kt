package com.babylon.orbit2.sample.calculator

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Suppress("unused")
class CalculatorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CalculatorApplication)
            modules(listOf(mainModule))
        }
    }

    private val mainModule = module {
        viewModel { (savedStateHandle: SavedStateHandle) -> CalculatorViewModel(savedStateHandle) }
    }
}
