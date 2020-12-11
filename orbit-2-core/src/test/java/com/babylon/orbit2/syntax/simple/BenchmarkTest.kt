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

package com.babylon.orbit2.syntax.simple

import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.system.measureTimeMillis

internal class BenchmarkTest {

    @Test
    fun benchmark() {
        val x = 100_000
        val middleware = BenchmarkMiddleware(x)
        val testFlowObserver = middleware.container.stateFlow.test()

        val actions = List(100_000) { Random.nextInt() }

        GlobalScope.launch {
            actions.forEach {
                middleware.reducer(it)
            }
        }

        val millisReducing = measureTimeMillis {
            middleware.latch.await(10, TimeUnit.SECONDS)
        }

        println(testFlowObserver.values.size)
        println(millisReducing)
        val reduction: Float = millisReducing.toFloat() / x
        println(reduction)
    }

    private data class TestState(val id: Int)

    private class BenchmarkMiddleware(count: Int) : ContainerHost<TestState, String> {
        override val container =
            CoroutineScope(Dispatchers.Unconfined).container<TestState, String>(TestState(42))

        val latch = CountDownLatch(count)

        fun reducer(action: Int) = intent {
            reduce {
                state.copy(id = action).also { latch.countDown() }
            }
        }
    }
}
