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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.appmattus.kotlinfixture.kotlinFixture
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.OrbitDslPlugins
import com.babylon.orbit2.assert
import com.babylon.orbit2.container
import com.babylon.orbit2.reduce
import com.babylon.orbit2.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
internal class LiveDataDslPluginBehaviourTest {
    private val fixture = kotlinFixture()
    private val initialState = fixture<TestState>()

    @BeforeEach
    fun beforeEach() {
        OrbitDslPlugins.reset() // Test for proper registration
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Test
    fun `livedata transformation flatmaps`() {
        val emission = fixture<Int>()
        val emission2 = fixture<Int>()
        val emission3 = fixture<Int>()
        val liveData = MutableLiveData<Int>()
        val middleware = Middleware(liveData).test(initialState = initialState, blocking = false)

        middleware.liveData()

        runBlocking {
            liveData.value = emission
            delay(150)
            liveData.value = emission2
            delay(150)
            liveData.value = emission3
        }

        middleware.assert {
            states(
                { TestState(emission) },
                { TestState(emission2) },
                { TestState(emission3) }
            )
        }
    }

    private data class TestState(val id: Int)

    private inner class Middleware(val liveData: LiveData<Int>) : ContainerHost<TestState, String> {

        override val container = CoroutineScope(Dispatchers.Unconfined).container<TestState, String>(TestState(42))

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
