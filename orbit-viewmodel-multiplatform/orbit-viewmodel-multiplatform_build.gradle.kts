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

plugins {
    // We should really be using the com.android.kotlin.multiplatform.library plugin and an androidLibrary block, however, this has issues with
    // AGP 8.9.0, and with 8.10.0-alpha08 no way to resolve packaging options such as META-INF conflicts.
    id("com.android.library")
    kotlin("multiplatform")
    id(libs.plugins.gradleMavenPublishPlugin.get().pluginId)
    id(libs.plugins.dokkaPlugin.get().pluginId)
    kotlin("plugin.serialization")
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "orbit-viewmodel-multiplatform"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopTest by getting

        commonMain.dependencies {
            implementation(project(":orbit-core"))
            implementation(libs.lifecycleViewmodelSavedState)
            implementation(libs.lifecycleViewmodel)
            implementation(libs.lifecycleRuntime)
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

        androidUnitTest.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.robolectric)
            implementation(libs.androidxCoreTesting)
        }
    }
}

android {
    namespace = "org.orbitmvi.orbit.viewmodel.multiplatform"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
    }

    testOptions.unitTests.isIncludeAndroidResources = true
}
