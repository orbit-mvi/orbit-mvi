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
import com.appmattus.kotlinfixture.kotlinFixture
import com.babylon.orbit2.Container
import com.babylon.orbit2.RealContainer
import com.babylon.orbit2.sideEffect
import com.babylon.orbit2.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Suppress("EXPERIMENTAL_API_USAGE")
@ExtendWith(InstantTaskExecutorExtension::class)
internal class LiveDataDslPluginDslThreadingTest {

    companion object {
        const val BACKGROUND_THREAD_PREFIX = "IO"
    }

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val fixture = kotlinFixture()

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
        val action = fixture<Int>()

        val container = scope.createContainer()
        val sideEffects = container.sideEffectFlow.test()
        var threadName = ""

        container.orbit {
            transformLiveData {
                liveData {
                    threadName = Thread.currentThread().name
                    emit(action)
                }
            }
                .sideEffect { post(event) }
        }

        sideEffects.awaitCount(1)
        assertThat(threadName).startsWith(BACKGROUND_THREAD_PREFIX)
    }

    private data class TestState(val id: Int)

    private fun CoroutineScope.createContainer(): Container<TestState, Int> {
        return RealContainer(
            initialState = TestState(0),
            settings = Container.Settings(),
            parentScope = this,
            backgroundDispatcher = newSingleThreadContext(BACKGROUND_THREAD_PREFIX)
        )
    }
}
