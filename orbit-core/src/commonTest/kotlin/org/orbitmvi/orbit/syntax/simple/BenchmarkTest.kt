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

package org.orbitmvi.orbit.syntax.simple

// @ExperimentalCoroutinesApi
// internal class BenchmarkTest {
//
//    private val scope = CoroutineScope(Job())
//
//    @AfterTest
//    fun afterTest() {
//        scope.cancel()
//    }
//
//    @Test
//    fun benchmark() {
//        val x = 100_000
//        val middleware = BenchmarkMiddleware(x)
//        val testFlowObserver = middleware.container.stateFlow.test()
//
//        val actions = List(100_000) { Random.nextInt() }
//
//        GlobalScope.launch {
//            actions.forEach {
//                middleware.reducer(it)
//            }
//        }
//
//        val millisReducing = measureTimeMillis {
//            middleware.latch.await(10, TimeUnit.SECONDS)
//        }
//
//        println(testFlowObserver.values.size)
//        println(millisReducing)
//        val reduction: Float = millisReducing.toFloat() / x
//        println(reduction)
//    }
//
//    private data class TestState(val id: Int)
//
//    private inner class BenchmarkMiddleware(count: Int) : ContainerHost<TestState, String> {
//        override val container = scope.container<TestState, String>(TestState(42))
//
//        val latch = CountDownLatch(count)
//
//        fun reducer(action: Int) = intent {
//            reduce {
//                state.copy(id = action).also { latch.countDown() }
//            }
//        }
//    }
// }
