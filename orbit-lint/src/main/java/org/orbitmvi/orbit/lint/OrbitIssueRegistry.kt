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

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

/**
 * Registry for Orbit Multiplatform lint checks.
 */
public class OrbitIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = listOf(
        ContainerBackingFieldDetector.ISSUE,
        SubIntentPrivateDetector.ISSUE
    )

    override val api: Int = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "Orbit Multiplatform",
        feedbackUrl = "https://github.com/orbit-mvi/orbit-mvi/issues",
        contact = "https://github.com/orbit-mvi/orbit-mvi"
    )
}
