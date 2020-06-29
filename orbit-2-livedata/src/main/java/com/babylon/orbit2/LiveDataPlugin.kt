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

package com.babylon.orbit2

import androidx.lifecycle.LiveData
import com.babylon.orbit2.Container.Settings
import java.io.Closeable

/**
 * A [LiveData] of one-off side effects. Depending on the [Settings] this container has been
 * instantiated with, can support side effect caching when there are no listeners (default)
 */
val <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.sideEffectLiveData: LiveData<SIDE_EFFECT>
    get() = DelegatingLiveData(this.sideEffectStream)

/**
 * A [LiveData] of state updates. Emits the latest state upon subscription and serves only distinct
 * values (only changed states are emitted) by default.
 */
val <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.stateLiveData: LiveData<STATE>
    get() = object : LiveData<STATE>(this.currentState) {
        private var closeable: Closeable? = null

        override fun onActive() {
            closeable = this@stateLiveData.stateStream.observe {
                postValue(it)
            }
        }

        override fun onInactive() {
            closeable?.close()
        }
    }
