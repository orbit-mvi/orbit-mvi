/*
 * Copyright 2019 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.disposables.Disposable

internal fun Disposable.bindToLifecycle(lifecycleOwner: LifecycleOwner) {
    val initialState = lifecycleOwner.lifecycle.currentState
    val exitEvent = initialState.correspondingExitEvent
    lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == exitEvent || !source.lifecycle.currentState.isAtLeast(initialState)) {
                dispose()
                source.lifecycle.removeObserver(this)
            }
        }
    })
}

private val Lifecycle.State.correspondingExitEvent: Lifecycle.Event
    get() = when (this) {
        Lifecycle.State.DESTROYED -> error("Lifecycle is already destroyed")
        // in onCreate
        Lifecycle.State.INITIALIZED -> Lifecycle.Event.ON_DESTROY
        // in onStart
        Lifecycle.State.CREATED -> Lifecycle.Event.ON_STOP
        // in onResume
        Lifecycle.State.STARTED -> Lifecycle.Event.ON_PAUSE
        // between onResume and onPause
        Lifecycle.State.RESUMED -> Lifecycle.Event.ON_PAUSE
    }
