package com.babylon.orbit2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class RealContainerTest {
    private val scope = CoroutineScope(Dispatchers.IO)

    @AfterEach
    fun after() {
        scope.cancel()
    }

    @Test
    fun `state is not volatile`() {
        runBlocking {
            val container = scope.createContainer()

            val stateChangedMutex = Mutex(locked = true)
            val completionMutex = Mutex(locked = true)

            container.orbit {
                transform {
                    runBlocking {
                        val initialState = state

                        stateChangedMutex.withLock {
                            delay(10)
                            assertEquals(initialState, state)
                            completionMutex.unlock()
                        }
                    }
                }
            }

            container.orbit {
                reduce {
                    runBlocking {
                        delay(50)
                        state.copy(value = state.value + 1).also {
                            stateChangedMutex.unlock()
                        }
                    }
                }
            }

            withTimeout(500) {
                completionMutex.withLock { }
            }
        }
    }

    @Test
    fun `volatile state changes mid-flow`() {
        runBlocking {
            val container = scope.createContainer()

            val stateChangedMutex = Mutex(locked = true)
            val completionMutex = Mutex(locked = true)

            container.orbit {
                transform {
                    runBlocking {
                        val initialState = volatileState()

                        stateChangedMutex.withLock {
                            delay(10)
                            assertNotEquals(initialState, volatileState())
                            completionMutex.unlock()
                        }
                    }
                }
            }

            container.orbit {
                reduce {
                    runBlocking {
                        delay(50)
                        state.copy(value = state.value + 1).also {
                            stateChangedMutex.unlock()
                        }
                    }
                }
            }

            withTimeout(500) {
                completionMutex.withLock { }
            }
        }
    }

    private fun CoroutineScope.createContainer(): Container<TestState, Int> {
        return container(
            initialState = TestState(0)
        )
    }

    data class TestState(val value: Int)
}
