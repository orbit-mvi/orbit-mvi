/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":samples:orbit-posts-compose-multiplatform:shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinCoroutinesSwing)

    implementation(libs.jetbrainsComposeUiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "org.orbitmvi.orbit.sample.posts.compose.multiplatform.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.orbitmvi.orbit.sample.posts.compose.multiplatform"
            packageVersion = "1.0.0"
        }
    }
}
