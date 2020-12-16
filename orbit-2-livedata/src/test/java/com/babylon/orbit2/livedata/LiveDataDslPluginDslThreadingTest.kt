/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2.livedata

import androidx.lifecycle.liveData
import com.babylon.orbit2.Container
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.internal.RealContainer
import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.syntax.strict.reduce
import com.babylon.orbit2.syntax.strict.sideEffect
import com.babylon.orbit2.test
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
import kotlin.random.Random

@Suppress("EXPERIMENTAL_API_USAGE", "UNREACHABLE_CODE", "ControlFlowWithEmptyBody", "EmptyWhileBlock")
@ExtendWith(InstantTaskExecutorExtension::class)
internal class LiveDataDslPluginDslThreadingTest {

    private val scope = TestCoroutineScope(Job())

    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun after() {
        scope.cancel()
        scope.cleanupTestCoroutines()
        Dispatchers.resetMain()
    }

    @Test
    fun `livedata dsl function does not block the container from receiving further intents`() {
        val action = Random.nextInt()
        val containerHost = scope.createContainerHost()
        val sideEffects = containerHost.container.sideEffectFlow.test()
        val mutex = Mutex(locked = true)

        containerHost.orbit {
            transformLiveData {
                liveData {
                    mutex.unlock()
                    while (currentCoroutineContext().isActive) {
                    }
                    emit(1)
                }
            }
                .sideEffect { post(event) }
        }

        runBlocking {
            withTimeout(TIMEOUT) {
                mutex.withLock { }
                delay(20)
            }
        }
        containerHost.orbit {
            sideEffect { post(action) }
        }

        sideEffects.awaitCount(1)
        sideEffects.values.shouldContainExactly(action)
    }

    @Test
    fun `livedata dsl function does not block the reducer`() {
        val action = Random.nextInt()
        val containerHost = scope.createContainerHost()
        val sideEffects = containerHost.container.sideEffectFlow.test()
        val states = containerHost.container.stateFlow.test()
        val mutex = Mutex(locked = true)

        containerHost.orbit {
            transformLiveData {
                liveData {
                    mutex.unlock()
                    while (currentCoroutineContext().isActive) {
                    }

                    emit(1)
                }
            }
                .sideEffect { post(event) }
        }

        runBlocking {
            withTimeout(TIMEOUT) {
                mutex.withLock { }
                delay(20)
            }
        }
        containerHost.orbit {
            reduce { TestState(action) }
        }

        states.awaitCount(2)
        sideEffects.values.shouldBeEmpty()
        states.values.shouldContainExactly(TestState(42), TestState(action))
    }

    private data class TestState(val id: Int)

    private fun CoroutineScope.createContainerHost(): ContainerHost<TestState, Int> {
        return object : ContainerHost<TestState, Int> {
            override val container: Container<TestState, Int> = RealContainer(
                initialState = TestState(42),
                parentScope = this@createContainerHost,
                settings = Container.Settings()
            )
        }
    }

    companion object {
        const val TIMEOUT = 1000L
    }
}
