package com.babylon.orbit.launcher.screenlist.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.babylon.orbit.launcher.screenlist.business.OrbitLauncherReducer
import com.babylon.orbit.launcher.screenlist.business.OrbitLauncherSideEffect
import com.babylon.orbit.launcher.screenlist.business.OrbitLauncherTransformer
import com.babylon.orbit.launcher.screenlist.business.OrbitLauncherViewModel

@Suppress("UNCHECKED_CAST")
class OrbitLauncherViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        OrbitLauncherViewModel(
            transformers = OrbitLauncherTransformer(),
            reducers = OrbitLauncherReducer(),
            sideEffects = OrbitLauncherSideEffect(context)
        ) as T
}
