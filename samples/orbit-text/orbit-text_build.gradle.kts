/*
 * Copyright 2022-2025 Mikołaj Leszczyński & Appmattus Limited
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
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.orbitmvi.orbit.sample.text"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    compileSdk = 35
    defaultConfig {
        minSdk = 21
        targetSdk = 35
        applicationId = "org.orbitmvi.orbit.sample.text"
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":orbit-compose"))

    implementation(libs.androidxComposeRuntime)
    implementation(libs.androidxLifecycleRuntimeKtx)
    implementation(libs.androidxComposeUi)

    implementation(libs.androidxComposeFoundation)
    implementation(libs.androidxComposeMaterial)
    implementation(libs.kotlinCoroutines)
    implementation(libs.androidxLifecycleLivedata)
    implementation(libs.androidxAppcompat)
}
