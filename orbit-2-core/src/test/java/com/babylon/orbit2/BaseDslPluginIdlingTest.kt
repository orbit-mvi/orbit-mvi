package com.babylon.orbit2

import com.babylon.orbit2.idling.IdlingResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseDslPluginIdlingTest {

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
    fun `transform not idle when actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transform {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
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
    fun `transform idle when actively running with registration disabled`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transform(registerIdling = false) {
                    runBlocking {
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
    fun `transform idle after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transform {
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
    fun `sideEffect not idle when actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                sideEffect {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
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
    fun `sideEffect idle when actively running with registration disabled`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                sideEffect(registerIdling = false) {
                    runBlocking {
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
    fun `sideEffect idle after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                sideEffect {
                    runBlocking {
                        mutex.unlock()
                    }
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
    fun `reduce not idle when actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                reduce {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        state
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
    fun `reduce idle when actively running with registration disabled`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                reduce(registerIdling = false) {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        state
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
    fun `reduce idle after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                reduce {
                    runBlocking {
                        mutex.unlock()
                        state
                    }
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
        }

        override fun decrement() {
            counter--
        }

        override fun close() = Unit

        fun isIdle() = counter == 0
    }
}
