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

package org.orbitmvi.orbit.syntax.simple

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.idling.IdlingResource
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class SimpleDslIdlingTest {

    private val testIdlingResource = TestIdlingResource()

    @Test
    fun idle_when_nothing_running() = runTest {
        backgroundScope.createContainerHost()
        delay(50)

        assertTrue(testIdlingResource.isIdle())
    }

    @Test
    fun not_idle_when_actively_running() = runTest {
        val containerHost = backgroundScope.createContainerHost()

        val mutex = Mutex(locked = true)

        containerHost.intent {
            mutex.unlock()
            delay(50)
        }

        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertFalse(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun idle_when_actively_running_with_registration_disabled() = runTest {
        val containerHost = backgroundScope.createContainerHost()

        val mutex = Mutex(locked = true)

        containerHost.intent(registerIdling = false) {
            mutex.unlock()
            delay(50)
        }

        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun idle_after_running() = runTest {
        val containerHost = backgroundScope.createContainerHost()

        val mutex = Mutex(locked = true)

        containerHost.intent {
            mutex.unlock()
        }

        mutex.withLock {
            assertEventually {
                assertTrue { testIdlingResource.isIdle() }
            }
        }
    }

    private fun CoroutineScope.createContainerHost(): ContainerHost<TestState, Int> {
        return object : ContainerHost<TestState, Int> {
            override val container: Container<TestState, Int> = container(
                initialState = TestState(0),
                buildSettings = {
                    idlingRegistry = testIdlingResource
                },
            )
        }
    }

    data class TestState(val value: Int)

    class TestIdlingResource : IdlingResource {
        private val counter = atomic(0)

        override fun increment() {
            counter.incrementAndGet()
        }

        override fun decrement() {
            counter.decrementAndGet()
        }

        override fun close() = Unit

        fun isIdle() = counter.value == 0
    }

    companion object {
        private const val TIMEOUT = 5000L
    }

    private suspend fun assertEventually(timeout: Long = 2000L, block: suspend () -> Unit) {
        withContext(Dispatchers.Default) {
            withTimeout(timeout) {
                while (true) {
                    try {
                        block()
                        break
                    } catch (ignored: Throwable) {
                        yield()
                    }
                }
            }
        }
    }
}
