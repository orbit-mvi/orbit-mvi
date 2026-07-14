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

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

/**
 * Detector that checks if a Container in a ContainerHost has a backing field.
 */
public class ContainerBackingFieldDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                // Check if the class implements ContainerHost
                val isContainerHost = node.supers.any { it.qualifiedName == "org.orbitmvi.orbit.ContainerHost" }
                if (!isContainerHost) return

                // Find the container property
                val containerProperty = node.methods.firstOrNull { it.name == "getContainer" } ?: return

                // Check if the container property has a backing field
                // If the property has a getter defined, it likely doesn't have a backing field
                // If it doesn't have a getter, it likely has a backing field
                val hasBackingField = containerProperty.uastBody == null

                if (!hasBackingField) {
                    context.report(
                        issue = ISSUE,
                        scopeClass = node,
                        location = context.getNameLocation(node),
                        message = "Container property must have a backing field. Use 'override val container =" +
                            " container<STATE, SIDE_EFFECT>(...)' instead of a getter."
                    )
                }
            }
        }
    }

    public companion object {
        public val ISSUE: Issue = Issue.create(
            id = "ContainerMustHaveBackingField",
            briefDescription = "Container must have a backing field",
            explanation = """
                The container property in a ContainerHost implementation should have a backing field.

                Use:
                ```
                override val container = container<STATE, SIDE_EFFECT>(...)
                ```

                Instead of:
                ```
                override val container: Container<STATE, SIDE_EFFECT>
                    get() = ...
                ```
                """,
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = Implementation(
                ContainerBackingFieldDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
