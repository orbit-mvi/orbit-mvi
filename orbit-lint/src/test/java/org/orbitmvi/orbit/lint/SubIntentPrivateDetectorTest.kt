/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

class SubIntentPrivateDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = SubIntentPrivateDetector()

    override fun getIssues(): List<Issue> = listOf(SubIntentPrivateDetector.ISSUE)

    @Test
    fun `test private function with subIntent`() {
        lint().files(
            containerHostStub,
            containerStub,
            subIntentStub,
            syntaxStub,
            kotlin(
                """
                class GoodViewModel : ContainerHost<TestState, TestSideEffect> {
                    private suspend fun goodFunction() = subIntent {
                        // Do something
                    }
                }

                data class TestState(val value: String = "")
                sealed class TestSideEffect
                """
            ).indented()
        ).run().expectClean()
    }

    @Test
    fun `test non-private function with subIntent`() {
        lint().files(
            containerHostStub,
            containerStub,
            subIntentStub,
            syntaxStub,
            kotlin(
                """
                class BadViewModel : ContainerHost<TestState, TestSideEffect> {
                    suspend fun badFunction() = subIntent {
                        // Do something
                    }
                }

                data class TestState(val value: String = "")
                sealed class TestSideEffect
                """
            ).indented()
        ).run().expect(
            """
            src/BadViewModel.kt:2: Error: Functions using subIntent must be private. [SubIntentMustBePrivate]
                suspend fun badFunction() = subIntent {
                            ~~~~~~~~~~~
            1 errors, 0 warnings
            """
        )
    }

    @Test
    fun `test function with Job return type and subIntent`() {
        lint().files(
            containerHostStub,
            containerStub,
            subIntentStub,
            syntaxStub,
            kotlinxCoroutinesStub,
            kotlin(
                """
                class JobViewModel : ContainerHost<TestState, TestSideEffect> {
                    suspend fun badFunction(): Job = subIntent {
                        // Do something
                    }
                }

                data class TestState(val value: String = "")
                sealed class TestSideEffect
                """
            ).indented()
        ).run().expect(
            """
            src/JobViewModel.kt:2: Error: Functions using subIntent must be private. [SubIntentMustBePrivate]
                suspend fun badFunction(): Job = subIntent {
                            ~~~~~~~~~~~
            1 errors, 0 warnings
            """
        )
    }

    @Test
    fun `test function without subIntent`() {
        lint().files(
            containerHostStub,
            containerStub,
            kotlin(
                """
                class NoSubIntentViewModel : ContainerHost<TestState, TestSideEffect> {
                    suspend fun functionWithoutSubIntent() {
                        // Do something without subIntent
                    }
                }

                data class TestState(val value: String = "")
                sealed class TestSideEffect
                """
            ).indented()
        ).run().expectClean()
    }

    private val containerHostStub = kotlin(
        """
        public interface ContainerHost<STATE : Any, SIDE_EFFECT : Any> {
            public val container: Container<STATE, SIDE_EFFECT>
        }
        """
    ).indented()

    private val containerStub = kotlin(
        """
        public interface Container<STATE : Any, SIDE_EFFECT : Any>
        """
    ).indented()

    private val subIntentStub = kotlin(
        """
        public suspend fun ContainerHost<*, *>.subIntent(
            transformer: suspend Syntax<*, *>.() -> Unit,
        ): Unit {
            error("Stub")
        }
        """
    ).indented()

    private val syntaxStub = kotlin(
        """
        public class Syntax<STATE : Any, SIDE_EFFECT : Any>
        """
    ).indented()

    private val kotlinxCoroutinesStub = kotlin(
        """
        public interface Job
        """
    ).indented()
}
