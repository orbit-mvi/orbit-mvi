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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean

class TestContainer<STATE : Any, SIDE_EFFECT : Any>(
    initialState: STATE,
    private val isolateFlow: Boolean
) : RealContainer<STATE, SIDE_EFFECT>(
    initialState = initialState,
    settings = Container.Settings(),
    parentScope = CoroutineScope(Dispatchers.Unconfined),
    orbitDispatcher = Dispatchers.Unconfined,
    backgroundDispatcher = Dispatchers.Unconfined
) {
    private val dispatched = AtomicBoolean(false)

    override fun orbit(
        init: Builder<STATE, SIDE_EFFECT, Unit>.() -> Builder<STATE, SIDE_EFFECT, *>
    ) {
        if (!isolateFlow || dispatched.compareAndSet(false, true)) {
            runBlocking {
                collectFlow(init)
            }
        }
    }
}
