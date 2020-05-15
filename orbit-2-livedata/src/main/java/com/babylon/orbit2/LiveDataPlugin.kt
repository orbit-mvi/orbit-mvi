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
import java.io.Closeable

val <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.sideEffectLiveData: LiveData<SIDE_EFFECT>
    get() = DelegatingLiveData(this.sideEffect)

val <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.orbitLiveData: LiveData<STATE>
    get() = object : LiveData<STATE>(this.currentState) {
        private var closeable: Closeable? = null

        override fun onActive() {
            closeable = this@orbitLiveData.orbit.observe {
                postValue(it)
            }
        }

        override fun onInactive() {
            closeable?.close()
        }
    }
