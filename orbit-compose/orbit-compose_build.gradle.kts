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

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    // We should really be using the com.android.kotlin.multiplatform.library plugin and an androidLibrary block, however, this has issues with
    // AGP 8.9.0, and with 8.10.0-alpha08 no way to resolve packaging options such as META-INF conflicts.
    id("com.android.library")
    kotlin("multiplatform")
    id(libs.plugins.gradleMavenPublishPlugin.get().pluginId)
    id(libs.plugins.dokkaPlugin.get().pluginId)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()

    js {
        browser {
            testTask {
                // Disabled due to https://youtrack.jetbrains.com/issue/CMP-8235/
                enabled = false
            }
        }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                // Disabled due to https://youtrack.jetbrains.com/issue/CMP-8235/
                enabled = false
            }
        }
    }

    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64(),
        watchosSimulatorArm64(),
        watchosX64(),
        watchosArm32(),
        watchosArm64(),
        tvosSimulatorArm64(),
        tvosX64(),
        tvosArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "orbit-compose"
            isStatic = true
        }
    }

    linuxX64()

    // org.jetbrains.androidx.lifecycle:lifecycle-common:2.9.0 missing
    // linuxArm64()

    mingwX64()

    jvm("desktop")

    // Apply the default hierarchy again. It'll create, for example, the iosMain source set:
    applyDefaultHierarchyTemplate()

    sourceSets {
        val desktopTest by getting

        // compose-ui-test does not target all our supported platforms
        val composeUiTest by creating {
            dependsOn(commonTest.get())
        }
        androidUnitTest.get().dependsOn(composeUiTest)
        jsTest.get().dependsOn(composeUiTest)
        wasmJsTest.get().dependsOn(composeUiTest)
        macosX64Test.get().dependsOn(composeUiTest)
        macosArm64Test.get().dependsOn(composeUiTest)
        iosX64Test.get().dependsOn(composeUiTest)
        iosArm64Test.get().dependsOn(composeUiTest)
        iosSimulatorArm64Test.get().dependsOn(composeUiTest)

        commonMain.dependencies {
            api(project(":orbit-core"))
            api(libs.androidxLifecycleRuntimeCompose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.kotlinCoroutines)
            implementation(libs.kotlinCoroutinesTest)
        }

        composeUiTest.dependencies {
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        desktopTest.dependencies {
            implementation(compose.desktop.currentOs)
        }

        androidUnitTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.robolectric)
            implementation(libs.androidxCoreTesting)
            implementation(libs.androidxComposeUiTestJunit4)
            implementation(libs.androidxComposeUiTestManifest)
        }

        wasmJsTest.dependencies {
            implementation(compose.ui)
        }
    }
}

android {
    namespace = "org.orbitmvi.orbit.compose"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }

    testOptions.unitTests.isIncludeAndroidResources = true
}
