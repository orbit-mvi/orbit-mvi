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
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class OrbitTestingTest {
    companion object {
        const val TIMEOUT = 500L
    }

    val fixture = kotlinFixture()

    @Suppress("unused")
    enum class BlockingModeTests(val blocking: Boolean) {
        BLOCKING(true),
        NON_BLOCKING(false)
    }

    @Nested
    inner class StateTests {

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `succeeds if emitted states match expected states`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = fixture<Int>()
            val action2 = fixture<Int>()

            testSubject.something(action)
            testSubject.something(action2)

            testSubject.assert(timeoutMillis = TIMEOUT) {
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
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = fixture<Int>()
            val action2 = fixture<Int>()

            testSubject.something(action)
            testSubject.something(action2)

            val throwable = assertThrows<AssertionError> {
                testSubject.assert(timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action) }
                    )
                }
            }

            assertThat(throwable.message).contains(
                "Expected 1 states but more were emitted:\n" +
                        "[State(count=$action2)]"
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if one more state expected than emitted`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = fixture<Int>()
            val action2 = fixture<Int>()
            val action3 = fixture<Int>()

            testSubject.something(action)
            testSubject.something(action2)

            val throwable = assertThrows<AssertionError> {
                testSubject.assert(timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action) },
                        { copy(count = action2) },
                        { copy(count = action3) }
                    )
                }
            }

            assertThat(throwable.message).contains(
                "Failed assertions at indices 2..2, expected states but never received:\n" +
                        "[State(count=$action3)]"
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if two more states expected than emitted`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = fixture<Int>()
            val action2 = fixture<Int>()
            val action3 = fixture<Int>()
            val action4 = fixture<Int>()

            testSubject.something(action)
            testSubject.something(action2)

            val throwable = assertThrows<AssertionError> {
                testSubject.assert(timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action) },
                        { copy(count = action2) },
                        { copy(count = action3) },
                        { copy(count = action4) }
                    )
                }
            }

            assertThat(throwable.message).contains(
                "Failed assertions at indices 2..3, expected states but never received:\n" +
                        "[State(count=$action3), State(count=$action4)]"
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if first emitted state does not match expected`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = State(),
                blocking = testCase.blocking
            )
            val action = fixture<Int>()
            val action2 = fixture<Int>()
            val action3 = fixture<Int>()

            testSubject.something(action)
            testSubject.something(action2)

            val throwable = assertThrows<AssertionError> {
                testSubject.assert(timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action2) },
                        { copy(count = action3) }
                    )
                }
            }

            assertThat(throwable.message).contains(
                "Failed assertion at index 0. " +
                        "Expected <State(count=$action2)>, actual <State(count=$action)>."
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if second emitted state does not match expected`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = State(),
                blocking = testCase.blocking
            )
            val action = fixture<Int>()
            val action2 = fixture<Int>()
            val action3 = fixture<Int>()

            testSubject.something(action)
            testSubject.something(action2)

            val throwable = assertThrows<AssertionError> {
                testSubject.assert(timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action2) },
                        { copy(count = action3) }
                    )
                }
            }

            assertThat(throwable.message).contains(
                "Failed assertion at index 0. " +
                        "Expected <State(count=$action2)>, actual <State(count=$action)>."
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if expected states are out of order`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = State(),
                blocking = testCase.blocking
            )
            val action = fixture<Int>()
            val action2 = fixture<Int>()

            testSubject.something(action)
            testSubject.something(action2)

            val throwable = assertThrows<AssertionError> {
                testSubject.assert(timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = action2) },
                        { copy(count = action) }
                    )
                }
            }

            assertThat(throwable.message).contains(
                "Failed assertion at index 0. " +
                        "Expected <State(count=$action2)>, actual <State(count=$action)>."
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `succeeds with dropped assertions`(testCase: BlockingModeTests) {
            val testSubject = StateTestMiddleware().test(
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = fixture<Int>()
            val action2 = fixture<Int>()
            val action3 = fixture<Int>()

            testSubject.something(action)
            testSubject.something(action2)
            testSubject.something(action3)

            testSubject.assert(timeoutMillis = TIMEOUT) {
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
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val action = fixture<Int>()
            val action2 = fixture<Int>()

            testSubject.something(action)
            testSubject.something(action2)

            val throwable = assertThrows<AssertionError> {
                testSubject.assert(timeoutMillis = TIMEOUT) {
                    states(
                        { copy(count = 0) },
                        { copy(count = action) }
                    )
                }
            }

            assertThat(throwable.message).contains(
                "Expected 2 states but more were emitted:\n" +
                        "[State(count=$action2)]\n\n" +
                        "Caution: 1 assertions were dropped as they encountered a current state " +
                        "which already satisfied them."
            )
        }

        private inner class StateTestMiddleware :
            ContainerHost<State, Nothing> {
            override val container =
                CoroutineScope(Dispatchers.Unconfined).container<State, Nothing>(State())

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
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val sideEffects = fixture<List<Int>>()

            sideEffects.forEach { testSubject.something(it) }

            testSubject.assert(timeoutMillis = TIMEOUT) {
                postedSideEffects(sideEffects)
            }
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if posted side effects do not match expected side effects`(testCase: BlockingModeTests) {
            val testSubject = SideEffectTestMiddleware().test(
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val sideEffects = fixture<List<Int>>()
            val sideEffects2 = fixture<List<Int>>()

            sideEffects.forEach { testSubject.something(it) }

            val throwable = assertThrows<AssertionError> {
                testSubject.assert(timeoutMillis = TIMEOUT) {
                    postedSideEffects(sideEffects2)
                }
            }

            assertThat(throwable.message).contains(
                "Expected <$sideEffects2>, actual <$sideEffects>."
            )
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `succeeds if loopbacks match`(testCase: BlockingModeTests) {
            val testSubject = SideEffectTestMiddleware().test(
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val sideEffects = fixture<List<Int>>()

            sideEffects.forEach { testSubject.something(it) }

            testSubject.assert(timeoutMillis = TIMEOUT) {
                postedSideEffects(sideEffects)

                sideEffects.forEach {
                    loopBack { somethingElse(it.toString()) }
                }
            }
        }

        @ParameterizedTest
        @EnumSource(BlockingModeTests::class)
        fun `fails if loopbacks do not match`(testCase: BlockingModeTests) {
            val testSubject = SideEffectTestMiddleware().test(
                initialState = State(),
                isolateFlow = false,
                blocking = testCase.blocking
            )
            val sideEffects = fixture<List<Int>>()
            val sideEffects2 = fixture<List<Int>>()

            sideEffects.forEach { testSubject.something(it) }

            assertThrows<AssertionError> {
                testSubject.assert(timeoutMillis = TIMEOUT) {
                    postedSideEffects(sideEffects)

                    sideEffects2.forEach {
                        loopBack { somethingElse(it.toString()) }
                    }
                }
            }
        }

        private inner class SideEffectTestMiddleware :
            ContainerHost<State, Int> {
            override val container = CoroutineScope(Dispatchers.Unconfined).container<State, Int>(State())

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

            val mockDependency = mock<BogusDependency>()
            val testSubject = GeneralTestMiddleware(mockDependency)

            val spy = testSubject.test(State())

            verify(spy, never()).created()
        }

        @Test
        fun `created is invoked upon request`() {

            val mockDependency = mock<BogusDependency>()
            val testSubject = GeneralTestMiddleware(mockDependency)

            testSubject.test(initialState = State(), runOnCreate = true)

            verify(mockDependency).create()
        }

        @Test
        fun `first flow is isolated by default`() {

            val mockDependency = mock<BogusDependency>()
            val testSubject = GeneralTestMiddleware(mockDependency)

            val spy = testSubject.test(State())

            spy.something()

            verify(mockDependency).something1()
            verify(mockDependency, never()).something2()
        }

        private inner class GeneralTestMiddleware(private val dependency: BogusDependency) :
            ContainerHost<State, Nothing> {
            override val container =
                CoroutineScope(Dispatchers.Unconfined).container<State, Nothing>(State()) {
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

    private data class State(val count: Int = 0)

    private interface BogusDependency {
        fun create()
        fun something1()
        fun something2()
    }
}
