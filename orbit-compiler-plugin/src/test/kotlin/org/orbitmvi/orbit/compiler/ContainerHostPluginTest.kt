/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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
 */

package org.orbitmvi.orbit.compiler

import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orbitmvi.orbit.logger.DebugLogger
import org.orbitmvi.orbit.logger.Logger
import org.orbitmvi.orbit.OrbitSettings
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.compiler.compile.compile
import org.orbitmvi.orbit.test.TestSettings
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCompilerApi::class, OrbitExperimental::class, ExperimentalCoroutinesApi::class)
class ContainerHostPluginTest {

    val logEntries = mutableListOf<LogEntry>()

    interface CompilationResult<T> {
        val containerHost: T
        val intentNames: List<String>
        val containerHostNames: List<String>
    }

    enum class LogType { State, SideEffect }

    data class LogEntry(val type: LogType, val containerHostName: String, val intentName: String, val value: Any)

    @BeforeEach
    fun setUp() {
        OrbitSettings.loggers = listOf(
            DebugLogger(),
            object : Logger {
                override fun logState(containerHostName: String, intentName: String, state: Any) {
                    logEntries.add(LogEntry(LogType.State, containerHostName, intentName, state))
                }

                override fun logSideEffect(containerHostName: String, intentName: String, sideEffect: Any) {
                    logEntries.add(LogEntry(LogType.SideEffect, containerHostName, intentName, sideEffect))
                }
            }
        )
    }

    @AfterEach
    fun tearDown() {
        OrbitSettings.loggers = emptyList()
    }

    @Test
    fun standardContainerHostHasExpectedNamesInSource() {
        val result = "StandardContainerHost".compile<TestContainerHost>(ContainerHostPlugin())

        assertContentEquals(listOf("\"org.orbitmvi.StandardContainerHost\""), result.containerHostNames)

        assertContentEquals(
            listOf(
                // triggerReadyState
                "\"org.orbitmvi.StandardContainerHost.triggerReadyState(parameter = \" + parameter + ')'",
                // triggerReadyState -> intent -> reduce -> triggerLoadingState
                "\"org.orbitmvi.StandardContainerHost.triggerReadyState.$1.$1.$1()\"",
                // triggerReadyState -> intent -> reduce -> triggerSideEffect
                "\"org.orbitmvi.StandardContainerHost.triggerReadyState.$1.$1.$2()\""
            ), result.intentNames
        )
    }

    @Test
    fun standardContainerHostHasNoNamesInSourceWithNoPlugin() {
        val result = "StandardContainerHost".compile<TestContainerHost>()

        assertContentEquals(listOf("null"), result.containerHostNames)

        assertContentEquals(
            listOf(
                // triggerReadyState
                // triggerReadyState -> intent -> reduce -> triggerLoadingState
                // triggerReadyState -> intent -> reduce -> triggerSideEffect
                "null"
            ), result.intentNames
        )
    }

    @Test
    fun standardContainerHostHasExpectedNamesInExecution() {
        val containerHost = "StandardContainerHost".compile<TestContainerHost>(ContainerHostPlugin()).containerHost

        runTest {
            containerHost.test(this, settings = TestSettings()) {
                expectInitialState()
                logEntries.last().assertState<State.Loading>(
                    expectedContainerHostName = "org.orbitmvi.StandardContainerHost",
                    expectedIntentName = "org.orbitmvi.StandardContainerHost.<initial-state>"
                )

                invokeIntent { triggerReadyState(6) }
                val state = awaitState() as State.Ready
                logEntries.last().assertState<State.Ready>(
                    expectedContainerHostName = "org.orbitmvi.StandardContainerHost",
                    expectedIntentName = "org.orbitmvi.StandardContainerHost.triggerReadyState(parameter = 6)"
                )

                invokeIntent { state.triggerSideEffect() }
                awaitSideEffect()
                logEntries.last().assertSideEffect(
                    expectedContainerHostName = "org.orbitmvi.StandardContainerHost",
                    expectedIntentName = "org.orbitmvi.StandardContainerHost.triggerReadyState.$1.$1.$2()",
                    expectedSideEffect = 6
                )

                invokeIntent { state.triggerLoadingState() }
                awaitState()
                logEntries.last().assertState<State.Loading>(
                    expectedContainerHostName = "org.orbitmvi.StandardContainerHost",
                    expectedIntentName = "org.orbitmvi.StandardContainerHost.triggerReadyState.$1.$1.$1()"
                )
            }
        }
    }

    @Test
    fun standardContainerHostHasNoNamesInExecutionWithNoPlugin() {
        val containerHost = "StandardContainerHost".compile<TestContainerHost>().containerHost

        runTest {
            containerHost.test(this, settings = TestSettings()) {
                expectInitialState()
                logEntries.last().assertState<State.Loading>(
                    expectedContainerHostName = "<not-populated>",
                    expectedIntentName = "<not-populated>.<initial-state>"
                )

                invokeIntent { triggerReadyState(6) }
                val state = awaitState() as State.Ready
                logEntries.last().assertState<State.Ready>(
                    expectedContainerHostName = "<not-populated>",
                    expectedIntentName = "<not-populated>"
                )

                invokeIntent { state.triggerSideEffect() }
                awaitSideEffect()
                logEntries.last().assertSideEffect(
                    expectedContainerHostName = "<not-populated>",
                    expectedIntentName = "<not-populated>",
                    expectedSideEffect = 6
                )

                invokeIntent { state.triggerLoadingState() }
                awaitState()
                logEntries.last().assertState<State.Loading>(
                    expectedContainerHostName = "<not-populated>",
                    expectedIntentName = "<not-populated>"
                )
            }
        }
    }

    @Test
    fun namedContainerHostHasExpectedNamesInExecution() {
        val containerHost = "NamedContainerHost".compile<TestContainerHost>(ContainerHostPlugin()).containerHost

        runTest {
            containerHost.test(this, settings = TestSettings()) {
                expectInitialState()
                logEntries.last().assertState<State.Loading>(
                    expectedContainerHostName = "CustomContainerHostName",
                    expectedIntentName = "CustomContainerHostName.<initial-state>"
                )

                invokeIntent { triggerReadyState(6) }
                val state = awaitState() as State.Ready
                logEntries.last().assertState<State.Ready>(
                    expectedContainerHostName = "CustomContainerHostName",
                    expectedIntentName = "ReadyState(6)"
                )

                invokeIntent { state.triggerSideEffect() }
                awaitSideEffect()
                logEntries.last().assertSideEffect(
                    expectedContainerHostName = "CustomContainerHostName",
                    expectedIntentName = "SideEffect(6)",
                    expectedSideEffect = 6
                )

                invokeIntent { state.triggerLoadingState() }
                awaitState()
                logEntries.last().assertState<State.Loading>(
                    expectedContainerHostName = "CustomContainerHostName",
                    expectedIntentName = "LoadingState(6)"
                )
            }
        }
    }

    @Test
    fun anonymousInnerClassContainerHostHasExpectedNamesInSource() {
        val result = "AnonymousInnerClassContainerHost".compile<TestInnerContainerHost>(ContainerHostPlugin())

        assertContentEquals(listOf("\"org.orbitmvi.AnonymousInnerClassContainerHost.host.<no name provided>\""), result.containerHostNames)

        assertContentEquals(
            listOf(
                // triggerReadyState
                "\"org.orbitmvi.AnonymousInnerClassContainerHost.triggerReadyState(parameter = \" + parameter + ')'",
                // triggerReadyState -> intent -> reduce -> triggerLoadingState
                "\"org.orbitmvi.AnonymousInnerClassContainerHost.triggerReadyState.$1.$1.$1()\"",
                // triggerReadyState -> intent -> reduce -> triggerSideEffect
                "\"org.orbitmvi.AnonymousInnerClassContainerHost.triggerReadyState.$1.$1.$2()\""
            ), result.intentNames
        )
    }

    @Test
    fun anonymousInnerClassContainerHostHasExpectedNamesInExecution() {
        val containerHost = "AnonymousInnerClassContainerHost".compile<TestInnerContainerHost>(ContainerHostPlugin()).containerHost

        runTest {
            containerHost.host.test(this, settings = TestSettings()) {
                expectInitialState()
                logEntries.last().assertState<State.Loading>(
                    expectedContainerHostName = "org.orbitmvi.AnonymousInnerClassContainerHost.host.<no name provided>",
                    expectedIntentName = "org.orbitmvi.AnonymousInnerClassContainerHost.host.<no name provided>.<initial-state>"
                )

                invokeIntent { containerHost.triggerReadyState(6) }
                val state = awaitState() as State.Ready
                logEntries.last().assertState<State.Ready>(
                    expectedContainerHostName = "org.orbitmvi.AnonymousInnerClassContainerHost.host.<no name provided>",
                    expectedIntentName = "org.orbitmvi.AnonymousInnerClassContainerHost.triggerReadyState(parameter = 6)"
                )

                invokeIntent { state.triggerSideEffect() }
                awaitSideEffect()
                logEntries.last().assertSideEffect(
                    expectedContainerHostName = "org.orbitmvi.AnonymousInnerClassContainerHost.host.<no name provided>",
                    expectedIntentName = "org.orbitmvi.AnonymousInnerClassContainerHost.triggerReadyState.$1.$1.$2()",
                    expectedSideEffect = 6
                )

                invokeIntent { state.triggerLoadingState() }
                awaitState()
                logEntries.last().assertState<State.Loading>(
                    expectedContainerHostName = "org.orbitmvi.AnonymousInnerClassContainerHost.host.<no name provided>",
                    expectedIntentName = "org.orbitmvi.AnonymousInnerClassContainerHost.triggerReadyState.$1.$1.$1()"
                )
            }
        }
    }

    @Test
    fun innerClassContainerHostHasExpectedNamesInSource() {
        val result = "InnerClassContainerHost".compile<TestInnerContainerHost>(ContainerHostPlugin())
        println(result.containerHostNames)

        assertContentEquals(listOf("\"org.orbitmvi.InnerClassContainerHost.InnerHost\""), result.containerHostNames)

        assertContentEquals(
            listOf(
                // triggerReadyState
                "\"org.orbitmvi.InnerClassContainerHost.triggerReadyState(parameter = \" + parameter + ')'",
                // triggerReadyState -> intent -> reduce -> triggerLoadingState
                "\"org.orbitmvi.InnerClassContainerHost.triggerReadyState.$1.$1.$1()\"",
                // triggerReadyState -> intent -> reduce -> triggerSideEffect
                "\"org.orbitmvi.InnerClassContainerHost.triggerReadyState.$1.$1.$2()\""
            ), result.intentNames
        )
    }

    private inline fun <reified T> LogEntry.assertState(expectedContainerHostName: String, expectedIntentName: String) {
        assertEquals(LogType.State, type)
        assertEquals(expectedContainerHostName, containerHostName)
        assertEquals(expectedIntentName, intentName)
        assertIs<T>(value)
    }

    private fun LogEntry.assertSideEffect(expectedContainerHostName: String, expectedIntentName: String, expectedSideEffect: Int) {
        assertEquals(LogType.SideEffect, type)
        assertEquals(expectedContainerHostName, containerHostName)
        assertEquals(expectedIntentName, intentName)
        assertEquals(expectedSideEffect, value)
    }

    private fun assertContentEquals(expected: Iterable<String>, actual: Iterable<String>) {
        kotlin.test.assertContentEquals(expected.sorted(), actual.sorted())
    }
}
