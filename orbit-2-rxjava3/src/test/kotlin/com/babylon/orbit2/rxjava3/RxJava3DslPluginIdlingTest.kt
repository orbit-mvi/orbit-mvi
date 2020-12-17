package com.babylon.orbit2.rxjava3

import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.idling.IdlingResource
import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.test.assertEventually
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class RxJava3DslPluginIdlingTest {

    private val scope = TestCoroutineScope(Job())
    private val testIdlingResource = TestIdlingResource()

    @AfterEach
    fun after() {
        scope.cleanupTestCoroutines()
        scope.cancel()
    }

    @Test
    fun `idle when nothing running`() {
        runBlocking {
            scope.createContainerHost()
            delay(50)
        }

        testIdlingResource.isIdle().shouldBeTrue()
    }

    @Test
    fun `transformRx3Single not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Single {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Single.just(0)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    testIdlingResource.isIdle().shouldBeFalse()
                }
            }
        }
    }

    @Test
    fun `transformRx3Single idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Single(registerIdling = false) {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Single.just(0)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    testIdlingResource.isIdle().shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `transformRx3Single idle after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Single {
                    runBlocking {
                        mutex.unlock()
                        Single.just(0)
                    }
                }
            }

            mutex.withLock {
                assertEventually {
                    testIdlingResource.isIdle().shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `transformRx3Completable not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Completable {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Completable.complete()
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    testIdlingResource.isIdle().shouldBeFalse()
                }
            }
        }
    }

    @Test
    fun `transformRx3Completable idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Completable(registerIdling = false) {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Completable.complete()
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    testIdlingResource.isIdle().shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `transformRx3Completable idle after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Completable {
                    runBlocking {
                        mutex.unlock()
                        Completable.complete()
                    }
                }
            }

            mutex.withLock {
                assertEventually {
                    testIdlingResource.isIdle().shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `transformRx3Observable not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Observable(registerIdling = true) {
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
                    testIdlingResource.isIdle().shouldBeFalse()
                }
            }
        }
    }

    @Test
    fun `transformRx3Observable idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Observable(registerIdling = false) {
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
                    testIdlingResource.isIdle().shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `transformRx3Observable idle after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Observable(registerIdling = true) {
                    Observable.fromCallable {
                        mutex.unlock()
                    }
                }
            }

            mutex.withLock {
                assertEventually {
                    testIdlingResource.isIdle().shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `transformRx3Maybe not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Maybe {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Maybe.just(0)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    testIdlingResource.isIdle().shouldBeFalse()
                }
            }
        }
    }

    @Test
    fun `transformRx3Maybe idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Maybe(registerIdling = false) {
                    runBlocking {
                        mutex.unlock()
                        delay(50)
                        Maybe.just(0)
                    }
                }
            }

            withTimeout(TIMEOUT) {
                mutex.withLock {
                    testIdlingResource.isIdle().shouldBeTrue()
                }
            }
        }
    }

    @Test
    fun `transformRx3Maybe idle after running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformRx3Maybe {
                    runBlocking {
                        mutex.unlock()
                        Maybe.just(0)
                    }
                }
            }

            mutex.withLock {
                assertEventually {
                    testIdlingResource.isIdle().shouldBeTrue()
                }
            }
        }
    }

    private fun CoroutineScope.createContainerHost(): ContainerHost<TestState, Int> {
        return object : ContainerHost<TestState, Int> {
            override var container: Container<TestState, Int> = container(
                initialState = TestState(0),
                settings = Container.Settings(idlingRegistry = testIdlingResource)
            )
        }
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
        private const val TIMEOUT = 2000L
    }
}
