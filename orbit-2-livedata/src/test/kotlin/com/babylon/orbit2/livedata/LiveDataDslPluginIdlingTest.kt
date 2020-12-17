package com.babylon.orbit2.livedata

import androidx.lifecycle.liveData
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.idling.IdlingResource
import com.babylon.orbit2.syntax.strict.orbit
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
class LiveDataDslPluginIdlingTest {

    private val scope = TestCoroutineScope(Job())
    private val testIdlingResource = TestIdlingResource()

    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun after() {
        runBlocking {
            scope.cleanupTestCoroutines()
            scope.cancel()
            delay(50)
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `idle when nothing running`() {
        scope.createContainerHost()

        testIdlingResource.isIdle().shouldBeTrue()
    }

    @Test
    fun `transformLiveData not idle when actively running`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformLiveData(registerIdling = true) {
                    liveData<Nothing> {
                        mutex.unlock()
                        delay(100)
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
    fun `transformLiveData idle when actively running with registration disabled`() {
        runBlocking {
            val containerHost = scope.createContainerHost()

            val mutex = Mutex(locked = true)

            containerHost.orbit {
                transformLiveData(registerIdling = false) {
                    liveData<Nothing> {
                        mutex.unlock()
                        delay(50)
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

    private fun CoroutineScope.createContainerHost(): ContainerHost<TestState, Int> {
        return object : ContainerHost<TestState, Int> {
            override var container: Container<TestState, Int> = container(
                initialState = TestState(0),
                settings = Container.Settings(idlingRegistry = testIdlingResource)
            )
        }
    }

    private data class TestState(val value: Int)

    private class TestIdlingResource : IdlingResource {
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
