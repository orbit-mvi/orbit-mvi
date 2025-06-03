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
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.getContainingUMethod

/**
 * Detector that checks if functions using subIntent are private.
 */
public class SubIntentPrivateDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                // Check if the call is to subIntent
                if (node.methodName != "subIntent") return

                // Get the containing method
                val containingMethod = node.getContainingUMethod() ?: return

                // Check if the method is private
                val isPrivate = containingMethod.javaPsi.modifierList.hasModifierProperty("private")
                if (!isPrivate) {
                    context.report(
                        issue = ISSUE,
                        location = context.getNameLocation(containingMethod),
                        message = "Functions using subIntent must be private."
                    )
                }
            }
        }
    }

    public companion object {
        public val ISSUE: Issue = Issue.create(
            id = "SubIntentMustBePrivate",
            briefDescription = "Functions using subIntent must be private",
            explanation = """
                Functions that use subIntent should be declared as private.

                Use:
                ```
                private suspend fun myFunction() = subIntent { ... }
                ```

                Instead of:
                ```
                suspend fun myFunction() = subIntent { ... }
                ```
                """,
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = Implementation(
                SubIntentPrivateDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
