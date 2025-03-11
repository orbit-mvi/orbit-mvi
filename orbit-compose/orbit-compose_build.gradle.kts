/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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

plugins {
    id("com.android.library")
    kotlin("android")
    id(libs.plugins.gradleMavenPublishPlugin.get().pluginId)
    id(libs.plugins.dokkaPlugin.get().pluginId)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.orbitmvi.orbit.compose"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":orbit-core"))

    implementation(libs.androidxComposeRuntime)
    implementation(libs.androidxLifecycleRuntimeCompose)
    implementation(libs.androidxLifecycleRuntimeKtx)
    implementation(libs.androidxComposeUi)

    // Testing
    testImplementation(kotlin("test-junit"))
    testImplementation(project(":orbit-test"))
    testImplementation(project(":test-common"))
    testImplementation(libs.androidxEspressoCore)
    testImplementation(libs.robolectric)

    testImplementation(libs.kotlinTest)
    testImplementation(libs.kotlinCoroutinesTest)

    testImplementation(libs.androidxCoreTesting)
    testImplementation(libs.androidxComposeUiTestJunit4)
    testImplementation(libs.androidxComposeUiTestManifest)
}
