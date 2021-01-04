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

package org.orbitmvi.orbit.viewmodel

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource.ResourceCallback
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.idling.IdlingResource
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class AndroidIdlingResource : IdlingResource {

    private val counter: AtomicInteger = AtomicInteger(0)
    private val idle = AtomicBoolean(true)

    private val job = AtomicReference<Job>()

    private var resourceCallback: ResourceCallback? = null

    private val espressoIdlingResource = object : androidx.test.espresso.IdlingResource {
        private val uniqueId = UUID.randomUUID()
        override fun getName() = "orbit-mvi-$uniqueId"

        override fun isIdleNow() = idle.get()

        override fun registerIdleTransitionCallback(resourceCallback: ResourceCallback?) {
            this@AndroidIdlingResource.resourceCallback = resourceCallback
        }
    }

    init {
        IdlingRegistry.getInstance().register(espressoIdlingResource)
    }

    override fun increment() {
        if (counter.getAndIncrement() == 0) {
            job.get()?.cancel()
        }
        idle.set(false)
    }

    override fun decrement() {
        if (counter.decrementAndGet() == 0) {
            job.getAndSet(
                GlobalScope.launch {
                    delay(MILLIS_BEFORE_IDLE)
                    idle.set(true)
                    resourceCallback?.onTransitionToIdle()
                }
            )?.cancel()
        }
    }

    override fun close() {
        IdlingRegistry.getInstance().unregister(espressoIdlingResource)
    }

    companion object {
        private const val MILLIS_BEFORE_IDLE = 100L
    }
}
