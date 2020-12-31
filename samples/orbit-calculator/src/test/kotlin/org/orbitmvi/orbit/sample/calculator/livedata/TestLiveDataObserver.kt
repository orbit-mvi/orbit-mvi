/*
 * Copyright 2021 Mikolaj Leszczynski & Matthew Dolan
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

package org.orbitmvi.orbit.sample.calculator.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.test(lifecycleOwner: LifecycleOwner) =
    TestLiveDataObserver(lifecycleOwner, this)

class TestLiveDataObserver<T>(lifecycleOwner: LifecycleOwner, private val liveData: LiveData<T>) {
    private val _values = mutableListOf<T>()
    private val observer = Observer<T> {
        _values.add(it)
    }
    val values: List<T>
        get() = _values

    init {
        liveData.observe(lifecycleOwner, observer)
    }

    fun awaitCount(count: Int, timeout: Long = 5000L) {
        val start = System.currentTimeMillis()
        while (values.count() < count) {
            if (System.currentTimeMillis() - start > timeout) {
                break
            }
            Thread.sleep(10)
        }
    }

    fun awaitIdle(timeout: Long = 10L) {
        var currentCount = values.count()

        while (true) {
            Thread.sleep(timeout)

            if (values.count() == currentCount) {
                break
            }

            currentCount = values.count()
        }
    }

    fun close(): Unit = liveData.removeObserver(observer)
}
