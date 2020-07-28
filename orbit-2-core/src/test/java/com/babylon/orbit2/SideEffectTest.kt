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

import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

internal class SideEffectTest {

    private val fixture = kotlinFixture()

    object MulticastTestCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
            Stream.of(
                Arguments.of(true),
                Arguments.of(false),
                Arguments.of(null)
            )
    }

    @ParameterizedTest(name = "Caching is {0}")
    @ArgumentsSource(MulticastTestCases::class)
    fun `side effects are multicast to all current observers by default`(caching: Boolean?) {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware(caching)

        val testSideEffectObserver1 = middleware.container.sideEffectStream.test()
        val testSideEffectObserver2 = middleware.container.sideEffectStream.test()
        val testSideEffectObserver3 = middleware.container.sideEffectStream.test()

        middleware.someFlow(action)
        middleware.someFlow(action2)
        middleware.someFlow(action3)

        testSideEffectObserver1.awaitCount(3)
        testSideEffectObserver2.awaitCount(3)
        testSideEffectObserver3.awaitCount(3)

        assertThat(testSideEffectObserver1.values).containsExactly(action, action2, action3)
        assertThat(testSideEffectObserver2.values).containsExactly(action, action2, action3)
        assertThat(testSideEffectObserver3.values).containsExactly(action, action2, action3)
    }

    object CachingOnTestCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
            Stream.of(
                Arguments.of(null),
                Arguments.of(true)
            )
    }

    @ParameterizedTest(name = "Caching is {0}")
    @ArgumentsSource(CachingOnTestCases::class)
    fun `when caching is turned on side effects are cached when there are no subscribers`(caching: Boolean?) {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware(caching)

        middleware.someFlow(action)
        middleware.someFlow(action2)
        middleware.someFlow(action3)

        val testSideEffectObserver1 = middleware.container.sideEffectStream.test()

        testSideEffectObserver1.awaitCount(3)

        assertThat(testSideEffectObserver1.values).containsExactly(action, action2, action3)
    }

    @Test
    fun `when caching is turned off side effects are not cached when there are no subscribers`() {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware(false)
        val testSideEffectObserver1 = middleware.container.sideEffectStream.test()

        middleware.someFlow(action)
        middleware.someFlow(action2)
        middleware.someFlow(action3)
        testSideEffectObserver1.awaitCount(3)
        testSideEffectObserver1.close()

        val testSideEffectObserver2 = middleware.container.sideEffectStream.test()

        testSideEffectObserver1.awaitCount(3, 10L)

        assertThat(testSideEffectObserver2.values).isEmpty()
    }

    @ParameterizedTest(name = "Caching is {0}")
    @ArgumentsSource(CachingOnTestCases::class)
    fun `when caching is turned on only new side effects are emitted when resubscribing`(caching: Boolean?) {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware(caching)

        val testSideEffectObserver1 = middleware.container.sideEffectStream.test()

        middleware.someFlow(action)

        testSideEffectObserver1.awaitCount(1)
        testSideEffectObserver1.close()

        middleware.someFlow(action2)
        middleware.someFlow(action3)

        val testSideEffectObserver2 = middleware.container.sideEffectStream.test()
        testSideEffectObserver2.awaitCount(2)

        assertThat(testSideEffectObserver1.values).containsExactly(action)
        assertThat(testSideEffectObserver2.values).containsExactly(action2, action3)
    }

    @ParameterizedTest(name = "Caching is {0}")
    @ArgumentsSource(CachingOnTestCases::class)
    fun `when caching is turned on new subscribers do not get updates if there is already a sub`(
        caching: Boolean?
    ) {
        val action = fixture<Int>()
        val action2 = fixture<Int>()
        val action3 = fixture<Int>()
        val middleware = Middleware(caching)

        val testSideEffectObserver1 = middleware.container.sideEffectStream.test()

        middleware.someFlow(action)
        middleware.someFlow(action2)
        middleware.someFlow(action3)

        testSideEffectObserver1.awaitCount(3)

        val testSideEffectObserver2 = middleware.container.sideEffectStream.test()

        assertThat(testSideEffectObserver1.values).containsExactly(action, action2, action3)
        assertThat(testSideEffectObserver2.values).isEmpty()
    }

    private class Middleware(caching: Boolean? = null) : ContainerHost<Unit, Int> {
        override val container: Container<Unit, Int> =
            with(CoroutineScope(Dispatchers.Unconfined)) {
                when (caching) {
                    null -> container(Unit) // making sure defaults are tested
                    else -> container(Unit, Container.Settings(caching))
                }
            }

        fun someFlow(action: Int) = orbit {
            sideEffect {
                post(action)
            }
        }
    }
}
