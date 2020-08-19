package com.babylon.orbit2.rxjava1

import com.babylon.orbit2.Container
import com.babylon.orbit2.container
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
import rx.Completable
import rx.Observable
import rx.Single

class RxJava1DslPluginIdlingTest {

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
    fun `transformRx1Single not idle when actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformRx1Single {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Single.just(0)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertFalse(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformRx1Single idle when actively running with registration disabled`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformRx1Single(registerIdling = false) {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Single.just(0)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformRx1Single idle after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformRx1Single {
                    runBlocking {
                        mutex.unlock()
                        Single.just(0)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                delay(50)
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformRx1Completable not idle when actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformRx1Completable {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Completable.complete()
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertFalse(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformRx1Completable idle when actively running with registration disabled`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformRx1Completable(registerIdling = false) {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Completable.complete()
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformRx1Completable idle after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformRx1Completable {
                    runBlocking {
                        mutex.unlock()
                        Completable.complete()
                    }
                }
            }

            withTimeout(TIMEOUT) {
                delay(50)
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformRx1Observable not idle when actively running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformRx1Observable(registerIdling = true) {
                    Observable.fromCallable {
                        runBlocking {
                            mutex.unlock()
                            delay(100)
                        }
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertFalse(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformRx1Observable idle when actively running with registration disabled`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformRx1Observable(registerIdling = false) {
                    Observable.fromCallable {
                        runBlocking {
                            mutex.unlock()
                            delay(100)
                        }
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    assertTrue(testIdlingResource.isIdle())
                }
            }
        }
    }

    @Test
    fun `transformRx1Observable idle after running`() {
        runBlocking {
            val container = scope.createContainer()

            val mutex = Mutex(locked = true)

            container.orbit {
                transformRx1Observable(registerIdling = true) {
                    Observable.fromCallable {
                        mutex.unlock()
                    }
                }
            }

            withTimeout(TIMEOUT) {
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
        }

        override fun decrement() {
            counter--
        }

        override fun close() = Unit

        fun isIdle() = counter == 0
    }

    companion object {
        private const val TIMEOUT = 500L
    }
}
