/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
}

android {
    namespace = "org.orbitmvi.orbit.sample.calculator"

    compileSdk = 35
    defaultConfig {
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        applicationId = "org.orbitmvi.orbit.sample.calculator"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        dataBinding = true
    }

    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":orbit-core"))
    implementation(project(":orbit-viewmodel"))

    implementation(libs.kotlinCoroutines)
    implementation(libs.androidxConstraintlayout)
    implementation(libs.googleMaterial)
    implementation(libs.koinAndroid)
    implementation(libs.androidxLifecycleLivedata)

    // Testing
    testImplementation(project(":orbit-test"))
    testRuntimeOnly(libs.junitJupiterEngine)
    testImplementation(libs.junitPlatformConsole)
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.junitJupiterParams)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinFixture)
    testImplementation(libs.kotlinCoroutinesTest)
}
