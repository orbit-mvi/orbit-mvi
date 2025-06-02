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

class ContainerBackingFieldDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = ContainerBackingFieldDetector()

    override fun getIssues(): List<Issue> = listOf(ContainerBackingFieldDetector.ISSUE)

    @Test
    fun `test container with backing field`() {
        lint().files(
            containerHostStub,
            containerStub,
            containerExtensionStub,
            kotlin(
                """
                package org.orbitmvi.orbit.sample

                import org.orbitmvi.orbit.Container
                import org.orbitmvi.orbit.ContainerHost
                import org.orbitmvi.orbit.container

                class GoodViewModel : ContainerHost<TestState, TestSideEffect> {
                    override val container = container<TestState, TestSideEffect>(TestState())
                }

                data class TestState(val value: String = "")
                sealed class TestSideEffect
                """
            ).indented()
        ).run().expectClean()
    }

    @Test
    fun `test container without backing field`() {
        lint().files(
            containerHostStub,
            containerStub,
            containerExtensionStub,
            kotlin(
                """
                package org.orbitmvi.orbit.sample

                import org.orbitmvi.orbit.Container
                import org.orbitmvi.orbit.ContainerHost
                import org.orbitmvi.orbit.container

                class BadViewModel : ContainerHost<TestState, TestSideEffect> {
                    override val container: Container<TestState, TestSideEffect>
                        get() = container<TestState, TestSideEffect>(TestState())
                }

                data class TestState(val value: String = "")
                sealed class TestSideEffect
                """
            ).indented()
        ).run().expect(
            """
            src/org/orbitmvi/orbit/sample/BadViewModel.kt:7: Error: Container property must have a backing field. Use 'override val container = container<STATE, SIDE_EFFECT>(...)' instead of a getter. [ContainerMustHaveBackingField]
            class BadViewModel : ContainerHost<TestState, TestSideEffect> {
                  ~~~~~~~~~~~~
            1 errors, 0 warnings
            """
        )
    }

    @Test
    fun `test non-container host class`() {
        lint().files(
            containerHostStub,
            containerStub,
            containerExtensionStub,
            kotlin(
                """
                package org.orbitmvi.orbit.sample

                class NonContainerHostClass {
                    val container: String
                        get() = "Not a container"
                }
                """
            ).indented()
        ).run().expectClean()
    }

    private val containerHostStub = kotlin(
        """
    package org.orbitmvi.orbit

    // Stub for lint unit tests
    public interface ContainerHost<STATE : Any, SIDE_EFFECT : Any> {
        public val container: Container<STATE, SIDE_EFFECT>
    }

    """
    ).indented()

    private val containerStub = kotlin(
        """
    package org.orbitmvi.orbit

    // Stub for lint unit tests
    public interface Container<STATE : Any, SIDE_EFFECT : Any>

    """
    ).indented()

    private val containerExtensionStub = kotlin(
        """
    package org.orbitmvi.orbit

    // Stub for lint unit tests
    public fun <STATE : Any, SIDE_EFFECT : Any> container(
        initialState: STATE,
        buildSettings: SettingsBuilder.() -> Unit = {},
        onCreate: (suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit)? = null
    ): Container<STATE, SIDE_EFFECT> {
        error("Stub")
    }

    """
    ).indented()

}
