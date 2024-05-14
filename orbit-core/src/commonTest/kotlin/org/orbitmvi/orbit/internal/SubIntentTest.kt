/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.subIntent
import org.orbitmvi.orbit.test.test
import kotlin.random.Random
import kotlin.test.Test

internal class SubIntentTest {

    @Test
    fun `sub intents can be executed in parallel`() = runTest {
        val initialState = TestState()
        val channel1 = Channel<String>()
        val channel2 = Channel<String>()
        Middleware2(
            scope = backgroundScope,
            initialState = initialState,
            flow1 = channel1.consumeAsFlow(),
            flow2 = channel2.consumeAsFlow()
        ).test(
            this,
        ) {
            expectInitialState()
            val job = runOnCreate()

            val str1 = Random.nextInt().toString()
            channel1.send(str1)

            expectSideEffect(str1)

            val str2 = Random.nextInt().toString()
            channel2.send(str2)
            expectSideEffect(str2)
            job.cancel()
        }
    }

    private data class TestState(val id: Int = Random.nextInt())
    private inner class Middleware2(
        scope: CoroutineScope,
        initialState: TestState,
        private val flow1: Flow<String>,
        private val flow2: Flow<String>
    ) : ContainerHost<TestState, String> {
        override val container = scope.container<TestState, String>(initialState) {
            coroutineScope {
                launch {
                    sendSideEffect1()
                }
                launch {
                    sendSideEffect2()
                }
            }
        }

        @OptIn(OrbitExperimental::class)
        private suspend fun sendSideEffect1() = subIntent {
            flow1.collect {
                postSideEffect(it)
            }
        }

        @OptIn(OrbitExperimental::class)
        private suspend fun sendSideEffect2() = subIntent {
            flow2.collect {
                postSideEffect(it)
            }
        }
    }
}
