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
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.coroutines.transformSuspend
import org.orbitmvi.orbit.syntax.strict.orbit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidIdlingResourceTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @BeforeEach
    fun before() {
        IdlingRegistry.getInstance().apply {
            unregister(*resources.toTypedArray())
        }
    }

    @AfterEach
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

        idlingResource.isIdleNow.shouldBeTrue()
    }

    @Test
    fun `not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformSuspend {
                    mutex.unlock()
                    delay(200)
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex.withLock {
                    idlingResource.isIdleNow.shouldBeFalse()
                }
            }
        }
    }

    @Test
    fun `idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformSuspend(registerIdling = false) {
                    mutex.unlock()
                    delay(200)
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex.withLock {
                    idlingResource.isIdleNow.shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `not idle directly after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformSuspend {
                    mutex.unlock()
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex.withLock {
                    idlingResource.isIdleNow.shouldBeFalse()
                }
            }
        }
    }

    @Test
    fun `idle shortly after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformSuspend {
                    mutex.unlock()
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(200) {
                mutex.withLock {
                    val result = suspendCoroutine<Boolean> {
                        idlingResource.registerIdleTransitionCallback {
                            it.resume(true)
                        }
                    }

                    result.shouldBeTrue()
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

            containerHost.orbit {
                transformSuspend {
                    mutex1.unlock()
                    delay(200)
                }
            }

            containerHost.orbit {
                transformSuspend {
                    mutex2.unlock()
                    delay(200)
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex1.withLock {
                    mutex2.withLock {
                        idlingResource.isIdleNow.shouldBeFalse()
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

            containerHost.orbit {
                transformSuspend {
                    delay(50)
                }.transformSuspend(registerIdling = false) {
                    mutex1.unlock()
                }
            }

            containerHost.orbit {
                transformSuspend {
                    mutex2.unlock()
                    delay(200)
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex1.withLock {
                    mutex2.withLock {
                        idlingResource.isIdleNow.shouldBeFalse()
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

            containerHost.orbit {
                transformSuspend {
                    mutex1.unlock()
                }
            }

            containerHost.orbit {
                transformSuspend {
                    mutex2.unlock()
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(ASSERT_TIMEOUT) {
                mutex1.withLock {
                    mutex2.withLock {
                        idlingResource.isIdleNow.shouldBeFalse()
                    }
                }
            }
        }
    }

    @Test
    fun `idle shortly after two running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex1 = Mutex(locked = true)
            val mutex2 = Mutex(locked = true)

            containerHost.orbit {
                transformSuspend {
                    mutex1.unlock()
                }
            }

            containerHost.orbit {
                transformSuspend {
                    mutex2.unlock()
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(200) {
                mutex1.withLock {
                    mutex2.withLock {
                        val result = suspendCancellableCoroutine<Boolean> {
                            idlingResource.registerIdleTransitionCallback {
                                it.resume(true)
                            }
                        }

                        result.shouldBeTrue()
                    }
                }
            }
        }
    }

    @Test
    fun `cancelling scope removes IdlingResource`() {
        runBlocking {
            scope.createContainerHost()

            IdlingRegistry.getInstance().resources.size.shouldBe(1)

            scope.cancel()

            IdlingRegistry.getInstance().resources.size.shouldBe(0)
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
        const val ASSERT_TIMEOUT = 200L
    }
}
