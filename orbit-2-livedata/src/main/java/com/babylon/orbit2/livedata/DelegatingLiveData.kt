/*
 * Copyright 2020 Babylon Partners Limited
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

package com.babylon.orbit2.livedata

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow

/**
 * This class creates one LiveData per observer in order to defer to the behaviour of the [Flow]
 * when it comes to caching values. This ensures that side effect caching is properly
 * resolved while retaining the benefits of using LiveData in terms of main thread callbacks and
 * automatic unsubscription.
 */
internal class DelegatingLiveData<T>(private val flow: Flow<T>) : LiveData<T>() {
    private val closeables = mutableMapOf<Observer<in T>, LiveData<T>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        // Observe the internal MutableLiveData
        closeables[observer] = flow.asLiveData().also {
            it.observe(owner, observer)
        }
    }

    override fun observeForever(observer: Observer<in T>) {
        closeables[observer] = flow.asLiveData().also {
            it.observeForever(observer)
        }
    }

    override fun getValue(): T? = null

    override fun hasActiveObservers(): Boolean {
        return closeables.values.any { it.hasActiveObservers() }
    }

    override fun hasObservers(): Boolean {
        return closeables.values.any { it.hasObservers() }
    }

    override fun setValue(value: T) =
        throw UnsupportedOperationException("This operation is not supported.")

    @Suppress("UNCHECKED_CAST")
    override fun removeObserver(observer: Observer<in T>) {
        closeables[observer]?.removeObserver(observer)
        closeables.remove(observer)
    }

    override fun removeObservers(owner: LifecycleOwner) {
        closeables.values.forEach { it.removeObservers(owner) }
        closeables.clear()
    }
}
