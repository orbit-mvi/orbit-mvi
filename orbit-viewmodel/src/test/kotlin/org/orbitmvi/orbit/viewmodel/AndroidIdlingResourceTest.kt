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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent

class AndroidIdlingResourceTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @BeforeTest
    fun before() {
        IdlingRegistry.getInstance().apply {
            unregister(*resources.toTypedArray())
        }
    }

    @AfterTest
    fun after() {
        scope.cancel()

        // Await for the idling resource to unregister
        runBlocking {
            awaitIdlingResourceUnregistration()
        }
    }

    private suspend fun awaitIdlingResourceUnregistration(timeoutMillis: Long = 200L) {
        runCatching {
            withTimeout(timeoutMillis) {
                while (IdlingRegistry.getInstance().resources.isNotEmpty()) {
                    delay(20)
                }
            }
        }
    }

    @Test
    fun `idle when nothing running`() {
        scope.createContainerHost()

        val idlingResource = IdlingRegistry.getInstance().resources.first()

        assertTrue(idlingResource.isIdleNow)
    }

    @Test
    fun `not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.intent {
                mutex.unlock()
                delay(200)
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex.withLock {
                    assertFalse(idlingResource.isIdleNow)
                }
            }
        }
    }

    @Test
    fun `idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.intent(registerIdling = false) {
                mutex.unlock()
                delay(200)
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex.withLock {
                    assertTrue(idlingResource.isIdleNow)
                }
            }
        }
    }

    @Test
    fun `not idle directly after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.intent {
                mutex.unlock()
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex.withLock {
                    assertFalse(idlingResource.isIdleNow)
                }
            }
        }
    }

    @Test
    fun `idle shortly after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            val flow = callbackFlow {
                IdlingRegistry.getInstance().resources.first().registerIdleTransitionCallback {
                    // Triggered when resource goes from busy to idle
                    trySend(true)
                }

                awaitClose { }
            }

            containerHost.intent {
                mutex.unlock()
            }

            withTimeout(ASSERT_TIMEOUT) {
                mutex.withLock {
                    assertTrue(flow.first())
                }
            }
        }
    }

    @Test
    fun `not idle when two actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex1 = Mutex(locked = true)
            val mutex2 = Mutex(locked = true)

            containerHost.intent {
                mutex1.unlock()
                delay(200)
            }

            containerHost.intent {
                mutex2.unlock()
                delay(200)
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex1.withLock {
                    mutex2.withLock {
                        assertFalse(idlingResource.isIdleNow)
                    }
                }
            }
        }
    }

    @Test
    fun `not idle when one of two actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex1 = Mutex(locked = true)
            val mutex2 = Mutex(locked = true)

            containerHost.intent {
                delay(50)
                mutex1.unlock()
            }

            containerHost.intent {
                mutex2.unlock()
                delay(200)
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex1.withLock {
                    mutex2.withLock {
                        assertFalse(idlingResource.isIdleNow)
                    }
                }
            }
        }
    }

    @Test
    fun `not idle directly after two running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex1 = Mutex(locked = true)
            val mutex2 = Mutex(locked = true)

            containerHost.intent {
                mutex1.unlock()
            }

            containerHost.intent {
                mutex2.unlock()
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex1.withLock {
                    mutex2.withLock {
                        assertFalse(idlingResource.isIdleNow)
                    }
                }
            }
        }
    }

    @Test
    fun `idle shortly after two running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val flow = callbackFlow {
                IdlingRegistry.getInstance().resources.first().registerIdleTransitionCallback {
                    // Triggered when resource goes from busy to idle
                    trySend(true)
                }

                awaitClose { }
            }

            val mutex1 = Mutex(locked = true)
            val mutex2 = Mutex(locked = true)

            containerHost.intent {
                mutex1.unlock()
            }

            containerHost.intent {
                mutex2.unlock()
            }

            withTimeout(ASSERT_TIMEOUT) {
                mutex1.withLock {
                    mutex2.withLock {
                        assertTrue(flow.first())
                    }
                }
            }
        }
    }

    @Test
    fun `cancelling scope removes IdlingResource`() {
        runBlocking {
            val host = scope.createContainerHost()
            // Make sure idling resource is lazily initialised
            host.intent { }

            assertEquals(1, IdlingRegistry.getInstance().resources.size)

            scope.cancel()

            assertEquals(0, IdlingRegistry.getInstance().resources.size)
        }
    }

    private fun CoroutineScope.createContainerHost(): ContainerHost<TestState, Int> {
        return object : ContainerHost<TestState, Int> {
            override val container: Container<TestState, Int> = container(
                initialState = TestState(0),
                settings = Container.Settings(idlingRegistry = AndroidIdlingResource())
            )
        }
    }

    data class TestState(val value: Int)

    private companion object {
        const val ASSERT_TIMEOUT = 1000L
    }
}
