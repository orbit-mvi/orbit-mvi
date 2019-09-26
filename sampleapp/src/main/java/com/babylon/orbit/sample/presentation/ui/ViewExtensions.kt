package com.babylon.orbit.sample.presentation.ui

import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Detects clicks on [View] and throttles them, e.g. to prevent double-tap bugs.
 *
 * [Observable.throttleFirst] operates by default on the computation scheduler which is why we allow
 * a scheduler such as main to be passed in.
 */
@Suppress("MagicNumber")
@JvmOverloads
fun View.throttledClick(
    delayInMs: Long = 700,
    observeOn: Scheduler = AndroidSchedulers.mainThread()
): Observable<Unit> {
    return this.clicks()
        .throttleFirst(delayInMs, TimeUnit.MILLISECONDS)
        .observeOn(observeOn)
}

fun View.show(isVisible: Boolean = true) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}
