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

package com.babylon.orbit

import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.system.measureTimeMillis

internal class BenchmarkTest {

    @Test
    fun `reducer benchmark`() {
        val container = BaseOrbitContainer<TestState, Unit>(
            middleware(TestState(1)) {
                perform("reduction")
                    .on<Int>()
                    .reduce { TestState(event) }
            }
        )
        val testStreamObserver = container.orbit.test()
        val x = 100_000

        val actions = List(x) {
            Random.nextInt()
        }

        actions.forEach {
            container.sendAction(it)
        }

        val millisReducing = measureTimeMillis {
            testStreamObserver.awaitCount(x + 1)
        }

        println(millisReducing)
        val reduction = millisReducing.toDouble() / x
        println(reduction)
    }
}
