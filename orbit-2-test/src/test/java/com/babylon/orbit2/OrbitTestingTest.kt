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

import com.babylon.orbit2.syntax.strict.orbit
import com.babylon.orbit2.syntax.strict.reduce
import com.babylon.orbit2.syntax.strict.sideEffect
import com.babylon.orbit2.syntax.strict.transform
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.random.Random

class OrbitTestingTest {
    companion object {
        const val TIMEOUT = 5000L
    }

    @Suppress("unused")
    enum class BlockingModeTests(val blocking: Boolean) {
        BLOCKING(true),
        NON_BLOCKING(false)
    }

    private val initialState = State()

    @Nested
    inner class StateTests {

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `succeeds if initial state matches expected state`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )

            val testStateObserver = testSubject.container.stateFlow.test()
            testStateObserver.awaitCount(1)

            testSubject.assert(initialState)
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if initial state does not match expected state`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val someRandomState = State()

            val testStateObserver = testSubject.container.stateFlow.test()
            testStateObserver.awaitCount(1)

            val throwable = shouldThrow<AssertionError> {
                testSubject.assert(someRandomState)
            }

            throwable.message.shouldContain(
                "Expected <$someRandomState>, actual <$initialState>."
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `succeeds if emitted states match expected states`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = Random.nextInt()
            val action2 = Random.nextInt()

            val testStateObserver = testSubject.container.stateFlow.test()
            testSubject.something(action)
            testStateObserver.awaitCount(2)
            testSubject.something(action2)
            testStateObserver.awaitCount(3)

            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                states(
                    { copy(count = action) },
                    { copy(count = action2) }
                )
            }
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if more states emitted than expected`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = Random.nextInt()
            val action2 = Random.nextInt()

            val testStateObserver = testSubject.container.stateFlow.test()
            testSubject.something(action)
            testStateObserver.awaitCount(2)
            testSubject.something(action2)
            testStateObserver.awaitCount(3)

            val throwable = shouldThrow<AssertionError> {
                testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action) }
                    )
                }
            }

            throwable.message.shouldContain(
                "Expected 1 states but more were emitted:\n" +
                        "[State(count=$action2)]"
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if one more state expected than emitted`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = Random.nextInt()
            val action2 = Random.nextInt()
            val action3 = Random.nextInt()

            val testStateObserver = testSubject.container.stateFlow.test()
            testSubject.something(action)
            testStateObserver.awaitCount(2)
            testSubject.something(action2)
            testStateObserver.awaitCount(3)

            val throwable = shouldThrow<AssertionError> {
                testSubject.assert(initialState, timeoutMillis = 1000L) {
                    states(
                        { copy(count = action) },
                        { copy(count = action2) },
                        { copy(count = action3) }
                    )
                }
            }

            throwable.message.shouldContain(
                "Failed assertions at indices 2..2, expected states but never received:\n" +
                        "[State(count=$action3)]"
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if two more states expected than emitted`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = Random.nextInt()
            val action2 = Random.nextInt()
            val action3 = Random.nextInt()
            val action4 = Random.nextInt()

            val testStateObserver = testSubject.container.stateFlow.test()
            testSubject.something(action)
            testStateObserver.awaitCount(2)
            testSubject.something(action2)
            testStateObserver.awaitCount(3)

            val throwable = shouldThrow<AssertionError> {
                testSubject.assert(initialState, timeoutMillis = 1000L) {
                    states(
                        { copy(count = action) },
                        { copy(count = action2) },
                        { copy(count = action3) },
                        { copy(count = action4) }
                    )
                }
            }

            throwable.message.shouldContain(
                "Failed assertions at indices 2..3, expected states but never received:\n" +
                        "[State(count=$action3), State(count=$action4)]"
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if first emitted state does not match expected`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = Random.nextInt()
            val action2 = Random.nextInt()
            val action3 = Random.nextInt()

            val testStateObserver = testSubject.container.stateFlow.test()
            testSubject.something(action)
            testStateObserver.awaitCount(2)
            testSubject.something(action2)
            testStateObserver.awaitCount(3)

            val throwable = shouldThrow<AssertionError> {
                testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action2) },
                        { copy(count = action3) }
                    )
                }
            }

            throwable.message.shouldContain(
                "Failed assertion at index 0. " +
                        "Expected <State(count=$action2)>, actual <State(count=$action)>."
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if second emitted state does not match expected`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = Random.nextInt()
            val action2 = Random.nextInt()
            val action3 = Random.nextInt()

            val testStateObserver = testSubject.container.stateFlow.test()
            testSubject.something(action)
            testStateObserver.awaitCount(2)
            testSubject.something(action2)
            testStateObserver.awaitCount(3)

            val throwable = shouldThrow<AssertionError> {
                testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action2) },
                        { copy(count = action3) }
                    )
                }
            }

            throwable.message.shouldContain(
                "Failed assertion at index 0. " +
                        "Expected <State(count=$action2)>, actual <State(count=$action)>."
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if expected states are out of order`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = Random.nextInt()
            val action2 = Random.nextInt()

            val testStateObserver = testSubject.container.stateFlow.test()
            testSubject.something(action)
            testStateObserver.awaitCount(2)
            testSubject.something(action2)
            testStateObserver.awaitCount(3)

            val throwable = shouldThrow<AssertionError> {
                testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action2) },
                        { copy(count = action) }
                    )
                }
            }

            throwable.message.shouldContain(
                "Failed assertion at index 0. " +
                        "Expected <State(count=$action2)>, actual <State(count=$action)>."
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `succeeds with dropped assertions`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = Random.nextInt()
            val action2 = Random.nextInt()
            val action3 = Random.nextInt()

            val testStateObserver = testSubject.container.stateFlow.test()
            testSubject.something(action)
            testStateObserver.awaitCount(2)
            testSubject.something(action2)
            testStateObserver.awaitCount(3)
            testSubject.something(action3)
            testStateObserver.awaitCount(4)

            testSubject.assert(initialState, timeoutMillis = 2000L) {
                states(
                    { copy(count = action) },
                    { copy(count = action2) },
                    { copy(count = action2) },
                    { copy(count = action3) }
                )
            }
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if dropped assertions mean extra states are observed`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = Random.nextInt()
            val action2 = Random.nextInt()

            val testStateObserver = testSubject.container.stateFlow.test()
            testSubject.something(action)
            testStateObserver.awaitCount(2)
            testSubject.something(action2)
            testStateObserver.awaitCount(3)

            val throwable = shouldThrow<AssertionError> {
                testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                    states(
                        { initialState },
                        { copy(count = action) }
                    )
                }
            }

            throwable.message.shouldContain(
                "Expected 2 states but more were emitted:\n" +
                        "[State(count=$action2)]\n\n" +
                        "Caution: 1 assertions were dropped as they encountered a current state " +
                        "which already satisfied them."
            )
        }

        private inner class StateTestMiddleware :
            ContainerHost<State, Nothing> {
            override val container =
                CoroutineScope(Dispatchers.Unconfined).container<State, Nothing>(initialState)

            fun something(action: Int): Unit = orbit {
                transform {
                    action.toString()
                }
                    .reduce {
                        state.copy(count = event.toInt())
                    }
            }
        }
    }

    @Nested
    inner class SideEffectTests {
        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `succeeds if posted side effects match expected side effects`(testCase: BlockingModeTests) {
            val testSubject = SideEffectTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val sideEffects = List(Random.nextInt(1, 5)) { Random.nextInt() }

            sideEffects.forEach { testSubject.something(it) }

            testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                postedSideEffects(sideEffects)
            }
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if posted side effects do not match expected side effects`(testCase: BlockingModeTests) {
            val testSubject = SideEffectTestMiddleware().test(
                initialState = initialState,
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val sideEffects = List(Random.nextInt(1, 5)) { Random.nextInt() }
            val sideEffects2 = List(Random.nextInt(1, 5)) { Random.nextInt() }

            sideEffects.forEach { testSubject.something(it) }

            val throwable = shouldThrow<AssertionError> {
                testSubject.assert(initialState, timeoutMillis = TIMEOUT) {
                    postedSideEffects(sideEffects2)
                }
            }

            throwable.message.shouldContain(
                "Expected <$sideEffects2>, actual <$sideEffects>."
            )
        }

        private inner class SideEffectTestMiddleware :
            ContainerHost<State, Int> {
            override val container = CoroutineScope(Dispatchers.Unconfined).container<State, Int>(initialState)

            fun something(action: Int): Unit = orbit {
                sideEffect {
                    post(action)
                }
                    .sideEffect { somethingElse(action.toString()) }
            }

            fun somethingElse(action: String) = orbit {
                sideEffect {
                    println(action)
                }
            }
        }
    }

    @Nested
    inner class GeneralTests {

        @Test
        fun `created is not invoked by default`() {

            val mockDependency = FakeDependency()
            val testSubject = GeneralTestMiddleware(mockDependency)

            testSubject.test(initialState)

            mockDependency.createCallCount.shouldBe(0)
        }

        @Test
        fun `created is invoked upon request`() {

            val mockDependency = FakeDependency()
            val testSubject = GeneralTestMiddleware(mockDependency)

            testSubject.test(initialState = initialState, runOnCreate = true)

            mockDependency.createCallCount.shouldBe(1)
        }

        @Test
        fun `first flow is isolated by default`() {

            val mockDependency = FakeDependency()
            val testSubject = GeneralTestMiddleware(mockDependency)

            val spy = testSubject.test(initialState)

            spy.something()

            mockDependency.something1CallCount.shouldBe(1)
            mockDependency.something2CallCount.shouldBe(0)
        }

        private inner class GeneralTestMiddleware(private val dependency: BogusDependency) :
            ContainerHost<State, Nothing> {
            override val container =
                CoroutineScope(Dispatchers.Unconfined).container<State, Nothing>(initialState) {
                    created()
                }

            fun created() {
                dependency.create()
                println("created!")
            }

            fun something() = orbit {
                sideEffect { dependency.something1() }
                    .sideEffect { somethingElse() }
            }

            fun somethingElse() = orbit {
                sideEffect { dependency.something2() }
            }
        }
    }

    private data class State(val count: Int = Random.nextInt())

    private interface BogusDependency {
        fun create()
        fun something1()
        fun something2()
    }

    private class FakeDependency : BogusDependency {
        var createCallCount = 0
            private set
        var something1CallCount = 0
            private set
        var something2CallCount = 0
            private set

        override fun create() {
            createCallCount++
        }

        override fun something1() {
            something1CallCount++
        }

        override fun something2() {
            something2CallCount++
        }
    }
}
