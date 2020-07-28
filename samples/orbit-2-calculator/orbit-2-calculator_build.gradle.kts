/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        applicationId = "com.babylon.orbit.sample"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(project(":orbit-2-core"))
    implementation(project(":orbit-2-coroutines"))
    implementation(project(":orbit-2-livedata"))
    implementation(project(":orbit-2-viewmodel"))
    implementation(kotlin("stdlib-jdk8"))

    implementation(ProjectDependencies.androidxLifecycleComponents)
    implementation(ProjectDependencies.androidxLifecycleSavedState)
    implementation(ProjectDependencies.androidxAppCompat)
    implementation(ProjectDependencies.androidxConstrainLayout)
    kapt(ProjectDependencies.androidxLifecycleCompiler)
    implementation(ProjectDependencies.androidMaterial)
    implementation(ProjectDependencies.koinViewModel)

    // Testing
    GroupedDependencies.testsImplementation.forEach { testImplementation(it) }
    GroupedDependencies.testsRuntime.forEach { testRuntimeOnly(it) }
    testImplementation(ProjectDependencies.koinTest)
}
