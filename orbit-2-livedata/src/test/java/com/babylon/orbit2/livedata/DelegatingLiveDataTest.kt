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

import androidx.lifecycle.Lifecycle
import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class DelegatingLiveDataTest {
    private val fixture = kotlinFixture()
    private val mockLifecycleOwner = MockLifecycleOwner().also {
        it.dispatchEvent(Lifecycle.Event.ON_CREATE)
    }

    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Suppress("unused")
    internal enum class TestCase {
        INITIALIZED {
            override val state = Lifecycle.State.INITIALIZED
            override val expectedSubscription = false
        },
        CREATED {
            override val state = Lifecycle.State.CREATED
            override val expectedSubscription = false
        },
        STARTED {
            override val state = Lifecycle.State.STARTED
            override val expectedSubscription = true
        },
        RESUMED {
            override val state = Lifecycle.State.RESUMED
            override val expectedSubscription = true
        },
        DESTROYED {
            override val state = Lifecycle.State.DESTROYED
            override val expectedSubscription = false
        };

        abstract val state: Lifecycle.State
        abstract val expectedSubscription: Boolean
    }

    @ParameterizedTest
    @EnumSource(TestCase::class)
    fun `observer does not subscribe until onStart`(testCase: TestCase) {
        val channel = Channel<Int>()
        val action = fixture<Int>()
        mockLifecycleOwner.currentState = testCase.state
        val observer = DelegatingLiveData(channel.consumeAsFlow()).test(mockLifecycleOwner)

        GlobalScope.launch {
            channel.send(action)
        }

        if (testCase.expectedSubscription) {
            observer.awaitCount(1)
            assertThat(observer.values).containsExactly(action)
        } else {
            assertThat(observer.values).isEmpty()
        }
    }

    @Test
    fun `observer is unsubscribed after the lifecycle is stopped`() {
        val channel = Channel<Int>()

        val action = fixture<Int>()
        val action2 = fixture<Int>()

        val observer = DelegatingLiveData(channel.consumeAsFlow()).test(mockLifecycleOwner)

        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)

        channel.sendBlocking(action)

        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_PAUSE)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)

        assertThrows<CancellationException> {
            channel.sendBlocking(action2)
        }

        assertThat(observer.values).containsExactly(action)
    }

    @Test
    fun `the current value cannot be retrieved and returns nulls instead`() {
        val channel = Channel<Int>()
        val action = fixture<Int>()
        val liveData = DelegatingLiveData(channel.consumeAsFlow())
        val observer = liveData.test(mockLifecycleOwner)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)

        channel.sendBlocking(action)

        assertThat(observer.values).containsExactly(action)
        assertThat(liveData.value).isNull()
    }
}
