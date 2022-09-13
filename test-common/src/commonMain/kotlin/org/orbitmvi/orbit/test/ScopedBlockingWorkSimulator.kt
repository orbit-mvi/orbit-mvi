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

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
public class ScopedBlockingWorkSimulator(private val scope: CoroutineScope) {

    private val job = atomic<Job?>(null)

    init {
        scope.produce<Unit>(Dispatchers.Default) {
            awaitClose {
                job.value?.cancel()
            }
        }
    }

    @Suppress("ControlFlowWithEmptyBody", "EmptyWhileBlock")
    public fun simulateWork() {
        job.updateAndGet {
            if (it != null) {
                error("Can be invoked only once")
            }
            scope.launch(Dispatchers.Default) {
                while (currentCoroutineContext().isActive) {
                }
            }
        }.let {
            runBlocking(it!!) { it.join() }
        }
    }
}
