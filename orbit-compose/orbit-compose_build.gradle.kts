/*
 * Copyright 2021-2026 Mikołaj Leszczyński & Appmattus Limited
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

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id(libs.plugins.gradleMavenPublishPlugin.get().pluginId)
    id(libs.plugins.dokkaPlugin.get().pluginId)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    android {
        namespace = "org.orbitmvi.orbit.compose"
        compileSdk = 37
        minSdk = 23

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

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

    // Compose does not support Wasm-WASI as it's designed for non-browser, command-line environments with no native UI
//    wasmWasi {
//        nodejs()
//    }

    // Tier 1
    // Apple macOS hosts only:
    macosArm64() // Running tests
    iosSimulatorArm64() // Running tests
    iosArm64()

    // Tier 2
    linuxX64() // Running tests
    linuxArm64()
    // Apple macOS hosts only:
    watchosSimulatorArm64() // Running tests
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64() // Running tests
    tvosArm64()

    // Tier 3
    // No androidNative support
//    androidNativeArm32()
//    androidNativeArm64()
//    androidNativeX86()
//    androidNativeX64()
    mingwX64() // Running tests
    // Apple macOS hosts only:
    // No watchosDeviceArm64 support
//    watchosDeviceArm64()
    iosX64() // Running tests

    listOf(
        macosArm64(),
        iosSimulatorArm64(),
        iosArm64(),
        watchosSimulatorArm64(),
        watchosArm32(),
        watchosArm64(),
        tvosSimulatorArm64(),
        tvosArm64(),
//        watchosDeviceArm64(),
        iosX64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "orbit-compose"
            isStatic = true
        }
    }

    // Apply the default hierarchy again. It'll create, for example, the iosMain source set:
    applyDefaultHierarchyTemplate()

    sourceSets {
        // compose-ui-test does not target all our supported platforms
        val composeUiTest by creating {
            dependsOn(commonTest.get())
        }

        val androidHostTest by getting

        jvmTest.get().dependsOn(composeUiTest)
        androidHostTest.dependsOn(composeUiTest)
        jsTest.get().dependsOn(composeUiTest)
        wasmJsTest.get().dependsOn(composeUiTest)
        macosArm64Test.get().dependsOn(composeUiTest)
        iosSimulatorArm64Test.get().dependsOn(composeUiTest)

        commonMain.dependencies {
            api(project(":orbit-core"))
            api(libs.jetbrainsLifecycleRuntimeCompose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.kotlinCoroutines)
            implementation(libs.kotlinCoroutinesTest)
        }

        composeUiTest.dependencies {
            implementation(libs.jetbrainsComposeUiTest)
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(compose.desktop.currentOs)
        }

        androidHostTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.robolectric)
            implementation(libs.androidxCoreTesting)
            implementation(libs.androidxComposeUiTestJunit4)
            implementation(libs.androidxComposeUiTestManifest)
        }
    }

    tasks.named<KotlinNativeTest>("tvosSimulatorArm64Test") {
        failOnNoDiscoveredTests = false
    }

    tasks.named<KotlinNativeTest>("watchosSimulatorArm64Test") {
        failOnNoDiscoveredTests = false
    }
}
