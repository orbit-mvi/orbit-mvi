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

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    android {
        namespace = "org.orbitmvi.orbit.sample.posts.compose.multiplatform.shared"
        compileSdk = 37
        minSdk = 24

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        val androidHostTest by getting

        androidMain.dependencies {
            implementation(libs.jetbrainsComposeUiToolingPreview)

            implementation(libs.androidxActivityCompose)
            implementation(libs.ktorClientAndroid)
        }
        androidHostTest.dependencies {
            implementation(libs.robolectric)
            implementation(libs.androidxCoreTesting)
        }
        commonMain.dependencies {
            implementation(project(":orbit-compose"))
            implementation(project(":orbit-viewmodel"))
            implementation(libs.jetbrainsComposeRuntime)
            implementation(libs.jetbrainsComposeFoundation)
            implementation(libs.jetbrainsComposeMaterial3)
            implementation(libs.jetbrainsComposeUi)
            implementation(libs.jetbrainsComposeComponentsResources)
            implementation(libs.jetbrainsComposeUiToolingPreview)
            implementation(libs.jetbrainsLifecycleViewmodel)
            implementation(libs.jetbrainsLifecycleViewmodelCompose)
            implementation(libs.jetbrainsLifecycleViewmodelSavedState)
            implementation(libs.jetbrainsLifecycleRuntimeCompose)
            implementation(libs.androidxCollection)
            implementation(libs.jetbrainsComposeMaterialIconsCoreMultiplafrom)
            implementation(libs.jetbrainsNavigationComposeMultiplatform)

            implementation(libs.ktorClientCore)
            implementation(libs.ktorClientContentNegotiation)
            implementation(libs.ktorSerializationKotlinxJson)
            implementation(libs.coilCompose)
            implementation(libs.coilNetworkKtor3)

            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewmodel)
            implementation(libs.koinComposeViewmodelNavigation)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":orbit-test"))
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
        wasmJsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
        iosMain.dependencies {
            implementation(libs.ktorClientIos)
        }
        jvmMain.dependencies {
            implementation(libs.ktorClientCio)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.androidxComposeUiTooling)
}
