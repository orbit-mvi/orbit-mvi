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

internal class ContainerLifecycleTest {

    private val fixture = kotlinFixture()

    @Test
    fun `onCreate is called once after connecting to the container`() {
        val initialState = fixture<TestState>()
        val middleware = Middleware(initialState)
        val testStateObserver = middleware.container.stateFlow.test()
        val testSideEffectObserver = middleware.container.sideEffectFlow.test()

        testStateObserver.awaitCount(1)
        testSideEffectObserver.awaitCount(1)

        assertThat(testStateObserver.values).containsExactly(initialState)
        assertThat(testSideEffectObserver.values).containsExactly(initialState.id.toString())
    }

    private data class TestState(val id: Int)

    private class Middleware(initialState: TestState) : ContainerHost<TestState, String> {

        override val container =
            CoroutineScope(Dispatchers.Unconfined).container<TestState, String>(initialState) {
                onCreate(it)
            }

        private fun onCreate(createState: TestState) = orbit {
            sideEffect {
                post(createState.id.toString())
            }
        }
    }
}
