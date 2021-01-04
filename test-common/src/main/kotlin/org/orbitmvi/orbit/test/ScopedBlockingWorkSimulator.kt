/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Suppress("EXPERIMENTAL_API_USAGE")
public class ScopedBlockingWorkSimulator(private val scope: CoroutineScope) {

    private var job: Job? = null

    init {
        scope.produce<Unit>(Dispatchers.Unconfined) {
            awaitClose {
                job?.cancel()
            }
        }
    }

    @Suppress("ControlFlowWithEmptyBody", "EmptyWhileBlock")
    public fun simulateWork() {
        if (job != null) {
            throw IllegalStateException("Can be invoked only once")
        }
        job = scope.launch {
            while (currentCoroutineContext().isActive) {
            }
        }
        runBlocking(job!!) { job!!.join() }
    }
}
