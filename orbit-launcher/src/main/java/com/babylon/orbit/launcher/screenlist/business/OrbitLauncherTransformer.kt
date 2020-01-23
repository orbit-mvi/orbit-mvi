package com.babylon.orbit.launcher.screenlist.business

import android.os.HandlerThread
import com.babylon.orbit.launcher.view.OrbitView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.ServiceLoader

internal class OrbitLauncherTransformer {

    private val handlerThread by lazy {
        HandlerThread(THREAD).apply { if (!isAlive) start() }
    }

    fun loadScreens(actions: Observable<Any>): Observable<List<OrbitScreenState>> =
        actions
            .observeOn(AndroidSchedulers.from(handlerThread.looper))
            .map {
                ServiceLoader
                    .load(OrbitView::class.java)
                    .iterator()
                    .asSequence()
                    .toList()
                    .map {
                        OrbitScreenState(
                            it.javaClass,
                            it.owner
                        )
                    }
            }

    companion object {

        private const val THREAD = "OrbitLauncherTransformerThread"
    }
}
