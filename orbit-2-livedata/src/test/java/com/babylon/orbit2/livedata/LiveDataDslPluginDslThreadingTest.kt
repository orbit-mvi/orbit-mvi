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
import com.babylon.orbit2.syntax.strict.sideEffect
import com.babylon.orbit2.test
import io.kotest.matchers.string.shouldStartWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.random.Random

@Suppress("EXPERIMENTAL_API_USAGE")
@ExtendWith(InstantTaskExecutorExtension::class)
internal class LiveDataDslPluginDslThreadingTest {

    companion object {
        const val BACKGROUND_THREAD_PREFIX = "IO"
    }

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun after() {
        scope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `livedata transformation runs on IO dispatcher`() {
        val action = Random.nextInt()

        val containerHost = scope.createContainerHost()
        val sideEffects = containerHost.container.sideEffectFlow.test()
        var threadName = ""

        containerHost.orbit {
            transformLiveData {
                liveData {
                    threadName = Thread.currentThread().name
                    emit(action)
                }
            }
                .sideEffect { post(event) }
        }

        sideEffects.awaitCount(1)

        threadName.shouldStartWith(BACKGROUND_THREAD_PREFIX)
    }

    private data class TestState(val id: Int)

    private fun CoroutineScope.createContainerHost(): ContainerHost<TestState, Int> {
        return object : ContainerHost<TestState, Int> {
            override val container: Container<TestState, Int> = RealContainer(
                initialState = TestState(0),
                parentScope = this@createContainerHost,
                settings = Container.Settings(
                    backgroundDispatcher = newSingleThreadContext(BACKGROUND_THREAD_PREFIX)
                )
            )
        }
    }
}
