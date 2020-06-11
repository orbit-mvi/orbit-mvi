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

package com.babylon.orbit2

import androidx.lifecycle.Lifecycle
import com.appmattus.kotlinfixture.kotlinFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantTaskExecutorExtension::class)
internal class StateConnectionLiveDataPluginTest {

    private val fixture = kotlinFixture()
    private val mockLifecycleOwner = MockLifecycleOwner().also {
        it.dispatchEvent(Lifecycle.Event.ON_CREATE)
        it.dispatchEvent(Lifecycle.Event.ON_START)
    }

    @Test
    fun `initial state is emitted on connection`() {
        val initialState = fixture<TestState>()
        val middleware = Middleware(initialState)
        val testStateObserver =
            middleware.container.stateLiveData.test(mockLifecycleOwner)

        testStateObserver.awaitCount(1)

        assertThat(testStateObserver.values).containsExactly(initialState)
    }

    @Test
    fun `latest state is emitted on connection to separate live datas`() {
        val initialState = fixture<TestState>()
        val middleware = Middleware(initialState)
        val testStateObserver =
            middleware.container.stateLiveData.test(mockLifecycleOwner)
        val action = fixture<Int>()
        middleware.something(action)
        testStateObserver.awaitCount(2) // block until the state is updated

        val testStateObserver2 =
            middleware.container.stateLiveData.test(mockLifecycleOwner)
        testStateObserver2.awaitCount(1)

        assertThat(testStateObserver.values).containsExactly(
            initialState,
            TestState(action)
        )
        assertThat(testStateObserver2.values).containsExactly(
            TestState(
                action
            )
        )
    }

    @Test
    fun `latest state is emitted on connection to the same live data`() {
        val initialState = fixture<TestState>()
        val middleware = Middleware(initialState)
        val liveData = middleware.container.stateLiveData
        val testStateObserver = liveData.test(mockLifecycleOwner)
        val action = fixture<Int>()
        middleware.something(action)
        testStateObserver.awaitCount(2) // block until the state is updated

        val testStateObserver2 = liveData.test(mockLifecycleOwner)
        testStateObserver2.awaitCount(1)

        assertThat(testStateObserver.values).containsExactly(
            initialState,
            TestState(action)
        )
        assertThat(testStateObserver2.values).containsExactly(
            TestState(
                action
            )
        )
    }

    @Test
    fun `current state is set to the initial state after instantiation`() {
        val initialState = fixture<TestState>()
        val middleware =
            Middleware(initialState)

        assertThat(middleware.container.currentState).isEqualTo(initialState)
    }

    @Test
    fun `current state is up to date after modification`() {
        val initialState = fixture<TestState>()
        val middleware =
            Middleware(initialState)
        val action = fixture<Int>()
        val testStateObserver =
            middleware.container.stateLiveData.test(mockLifecycleOwner)

        middleware.something(action)

        testStateObserver.awaitCount(2)

        assertThat(middleware.container.currentState).isEqualTo(testStateObserver.values.last())
    }

    private data class TestState(val id: Int)

    private class Middleware(initialState: TestState) : Host<TestState, String> {
        override val container = Container.create<TestState, String>(initialState)

        fun something(action: Int) = orbit {
            reduce {
                state.copy(id = action)
            }
        }
    }
}
