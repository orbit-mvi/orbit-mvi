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

package org.orbitmvi.orbit.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.orbitmvi.orbit.assert
import org.orbitmvi.orbit.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugins
import org.orbitmvi.orbit.syntax.strict.orbit
import org.orbitmvi.orbit.syntax.strict.reduce
import kotlin.random.Random

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class LiveDataDslPluginBehaviourTest {
    private val initialState = TestState()
    private val scope = TestCoroutineScope(Job())

    @BeforeEach
    fun beforeEach() {
        OrbitDslPlugins.reset() // Test for proper registration
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun afterEach() {
        scope.cleanupTestCoroutines()
        scope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `livedata transformation flatmaps`() {
        val emission = Random.nextInt()
        val emission2 = Random.nextInt()
        val emission3 = Random.nextInt()
        val liveData = MutableLiveData<Int>()
        val middleware = Middleware(liveData).test(initialState = initialState, blocking = false)
        val testObserver = middleware.container.stateFlow.test()

        middleware.liveData()

        runBlocking {
            liveData.value = emission
            testObserver.awaitCount(2)
            liveData.value = emission2
            testObserver.awaitCount(3)
            liveData.value = emission3
        }

        middleware.assert(initialState) {
            states(
                { TestState(emission) },
                { TestState(emission2) },
                { TestState(emission3) }
            )
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware(val liveData: LiveData<Int>) : ContainerHost<TestState, String> {

        override val container = scope.container<TestState, String>(TestState(42))

        fun liveData() = orbit {
            transformLiveData {
                liveData
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
