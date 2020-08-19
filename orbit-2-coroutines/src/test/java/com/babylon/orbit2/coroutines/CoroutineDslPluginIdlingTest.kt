package com.babylon.orbit2.coroutines

import com.babylon.orbit2.Container
import com.babylon.orbit2.container
import com.babylon.orbit2.idling.IdlingResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CoroutineDslPluginIdlingTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val testIdlingResource = TestIdlingResource()

    @AfterEach
    fun after() {
        scope.cancel()
    }

    @Test
    fun `idle when nothing running`() {
        scope.createContainer()

        assertTrue(testIdlingResource.isIdle())
    }

    @Test
    fun `transformSuspend not idle when actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformSuspend {
                    mutex.unlock()
                    delay(50)
                }
            }

            withTimeout(100) {
                mutex.withLock {
                    assertFalse(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformSuspend idle when actively running with registration disabled`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformSuspend(registerIdling = false) {
                    mutex.unlock()
                    delay(50)
                }
            }

            withTimeout(100) {
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformSuspend idle after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformSuspend {
                    mutex.unlock()
                }
            }

            withTimeout(100) {
                delay(50)
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformFlow not idle when actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformFlow(registerIdling = true) {
                    flow<Int> {
                        println("start")
                        mutex.unlock()
                        println("release")
                        delay(100)
                        println("end")
                    }
                }
            }

            withTimeout(100) {
                mutex.withLock {
                    assertFalse(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformFlow idle when actively running with registration disabled`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformFlow(registerIdling = false) {
                    flow<Int> {
                        mutex.unlock()
                        delay(50)
                    }
                }
            }

            withTimeout(100) {
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformFlow idle after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformFlow(registerIdling = true) {
                    flow<Int> {
                        mutex.unlock()
                    }
                }
            }

            withTimeout(100) {
                mutex.withLock {
                    delay(50)
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    private fun CoroutineScope.createContainer(): Container<TestState, Int> {
        return container(
            initialState = TestState(0),
            settings = Container.Settings(idlingRegistry = testIdlingResource)
        )
    }

    data class TestState(val value: Int)

    class TestIdlingResource : IdlingResource {
        private var counter = 0

        override fun increment() {
            counter++
            println("inc")
        }

        override fun decrement() {
            counter--
            println("dec")
        }

        override fun close() = Unit

        fun isIdle() = counter == 0
    }
}
