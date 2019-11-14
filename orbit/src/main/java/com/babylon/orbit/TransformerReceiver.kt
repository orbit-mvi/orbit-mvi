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

package com.babylon.orbit

import io.reactivex.Observable

/**
 * @property eventObservable The original observable to be transformed.
 */
@OrbitDsl
class TransformerReceiver<STATE : Any, EVENT : Any>(
    private val stateProvider: () -> STATE,
    val eventObservable: Observable<EVENT>
) {
    /**
     * Returns the current state captured whenever this field is accessed. Successive queries of this
     * field may yield different results each time as the state could be modified by another flow at
     * any time.
     */
    val currentState
        get() = stateProvider()
}
