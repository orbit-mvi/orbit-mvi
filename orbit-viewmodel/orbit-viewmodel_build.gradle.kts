/*
 * Copyright 2021-2026 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id(libs.plugins.gradleMavenPublishPlugin.get().pluginId)
    id(libs.plugins.dokkaPlugin.get().pluginId)
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

kotlin {
    jvm()

    android {
        namespace = "org.orbitmvi.orbit.viewmodel"
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
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

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
            baseName = "orbit-viewmodel"
            isStatic = true
        }
    }

    // Apply the default hierarchy again. It'll create, for example, the iosMain source set:
    applyDefaultHierarchyTemplate()

    sourceSets {
        val androidHostTest by getting

        commonMain.dependencies {
            api(project(":orbit-core"))
            api(libs.jetbrainsLifecycleViewmodel)
            api(libs.jetbrainsLifecycleViewmodelSavedState)
            api(libs.jetbrainsLifecycleRuntime)
        }

        commonTest.dependencies {
            implementation(project(":orbit-test"))

            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.kotlinCoroutines)
            implementation(libs.kotlinCoroutinesTest)
            implementation(libs.turbine)
        }

        androidMain.dependencies {
            implementation(libs.androidxEspressoIdlingResource)
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
        }

        androidHostTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.robolectric)
            implementation(libs.androidxCoreTesting)
            implementation(libs.androidxEspressoCore)
        }
    }
}
