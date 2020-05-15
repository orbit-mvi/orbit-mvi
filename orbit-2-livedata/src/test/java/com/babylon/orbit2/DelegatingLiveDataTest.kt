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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.Closeable

@ExtendWith(InstantTaskExecutorExtension::class)
internal class DelegatingLiveDataTest {
    private val fixture = kotlinFixture()
    private val mockLifecycleOwner = MockLifecycleOwner().also {
        it.dispatchEvent(Lifecycle.Event.ON_CREATE)
    }

    class TestStream<T> : Stream<T> {
        private val observers = mutableSetOf<(T) -> Unit>()

        override fun observe(lambda: (T) -> Unit): Closeable {
            observers += lambda
            return Closeable { observers.remove(lambda) }
        }

        fun post(value: T) {
            observers.forEach { it(value) }
        }

        fun hasObservers() = observers.size > 0
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
        val stream = TestStream<Int>()
        val action = fixture<Int>()
        mockLifecycleOwner.currentState = testCase.state
        val observer = DelegatingLiveData(stream).test(mockLifecycleOwner)

        stream.post(action)

        if (testCase.expectedSubscription) {
            assertThat(stream.hasObservers()).isTrue()
            assertThat(observer.values).containsExactly(action)
        } else {
            assertThat(stream.hasObservers()).isFalse()
            assertThat(observer.values).isEmpty()
        }
    }

    @Test
    fun `observer is unsubscribed after the lifecycle is stopped`() {
        val stream = TestStream<Int>()

        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()

        val observer = DelegatingLiveData(stream).test(mockLifecycleOwner)

        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)

        stream.post(action)
        assertThat(stream.hasObservers()).isTrue()

        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_PAUSE)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)

        stream.post(action2)
        stream.post(action3)

        assertThat(observer.values).containsExactly(action)
        assertThat(stream.hasObservers()).isFalse()
    }

    @Test
    fun `the current value cannot be retrieved and returns nulls instead`() {
        val stream = TestStream<Int>()
        val action = fixture<Int>()
        val liveData = DelegatingLiveData(stream)
        val observer = liveData.test(mockLifecycleOwner)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        mockLifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)

        stream.post(action)

        assertThat(observer.values).containsExactly(action)
        assertThat(liveData.value).isNull()
    }
}
