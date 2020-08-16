package com.babylon.orbit2.viewmodel

import androidx.test.espresso.IdlingRegistry
import com.babylon.orbit2.Container
import com.babylon.orbit2.container
import com.babylon.orbit2.coroutines.transformSuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidIdlingResourceTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @AfterEach
    fun after() {
        scope.cancel()
    }

    @Test
    fun `idle when nothing running`() {
        scope.createContainer()

        val idlingResource = IdlingRegistry.getInstance().resources.first()

        assertTrue(idlingResource.isIdleNow)
    }

    @Test
    fun `not idle when actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformSuspend {
                    mutex.unlock()
                    delay(200)
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(100) {
                mutex.withLock {
                    assertFalse(idlingResource.isIdleNow)
                }
            }
        }
    }

    @Test
    fun `idle when actively running with registration disabled`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformSuspend(registerIdling = false) {
                    mutex.unlock()
                    delay(200)
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(100) {
                mutex.withLock {
                    assertTrue(idlingResource.isIdleNow)
                }
            }
        }
    }


    @Test
    fun `not idle directly after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformSuspend {
                    mutex.unlock()
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(100) {
                mutex.withLock {
                    assertFalse(idlingResource.isIdleNow)
                }
            }
        }
    }

    @Test
    fun `idle shortly after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
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

                    assertTrue(result)
                }
            }
        }
    }

    @Test
    fun `not idle when two actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex1 = Mutex(locked = true)
            val mutex2 = Mutex(locked = true)

            container.orbit {
                transformSuspend {
                    mutex1.unlock()
                    delay(200)
                }
            }

            container.orbit {
                transformSuspend {
                    mutex2.unlock()
                    delay(200)
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(100) {
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
            val container = scope.createContainer()

            val mutex1 = Mutex(locked = true)
            val mutex2 = Mutex(locked = true)

            container.orbit {
                transformSuspend {
                    delay(50)
                }.transformSuspend(registerIdling = false) {
                    mutex1.unlock()
                }
            }

            container.orbit {
                transformSuspend {
                    mutex2.unlock()
                    delay(200)
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(100) {
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
            val container = scope.createContainer()

            val mutex1 = Mutex(locked = true)
            val mutex2 = Mutex(locked = true)

            container.orbit {
                transformSuspend {
                    mutex1.unlock()
                }
            }

            container.orbit {
                transformSuspend {
                    mutex2.unlock()
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(100) {
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
            val container = scope.createContainer()

            val mutex1 = Mutex(locked = true)
            val mutex2 = Mutex(locked = true)

            container.orbit {
                transformSuspend {
                    mutex1.unlock()
                }
            }

            container.orbit {
                transformSuspend {
                    mutex2.unlock()
                }
            }

            val idlingResource = IdlingRegistry.getInstance().resources.first()

            withTimeout(200) {
                mutex1.withLock {
                    mutex2.withLock {
                        val result = suspendCoroutine<Boolean> {
                            idlingResource.registerIdleTransitionCallback {
                                it.resume(true)
                            }
                        }

                        assertTrue(result)
                    }
                }
            }
        }
    }


    private fun CoroutineScope.createContainer(): Container<TestState, Int> {
        return container(
            initialState = TestState(0),
            settings = Container.Settings(idlingRegistry = AndroidIdlingResource())
        )
    }

    @Test
    fun `cancelling scope removes IdlingResource`() {
        runBlocking {
            val scope = CoroutineScope(Dispatchers.Unconfined)

            scope.container<TestState, Int>(
                initialState = TestState(0),
                settings = Container.Settings(idlingRegistry = AndroidIdlingResource())
            )

            assertEquals(1, IdlingRegistry.getInstance().resources.size)

            scope.cancel()

            yield()

            assertEquals(0, IdlingRegistry.getInstance().resources.size)
        }
    }

    data class TestState(val value: Int)
}
