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
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        applicationId = "com.babylon.orbit2.sample.posts"
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":orbit-2-core"))
    implementation(project(":orbit-2-coroutines"))
    implementation(project(":orbit-2-livedata"))
    implementation(project(":orbit-2-viewmodel"))

    // Kotlin
    implementation(ProjectDependencies.kotlinCoroutines)

    // UI
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.arch.core:core-runtime:2.1.0")
    implementation("androidx.arch.core:core-common:2.1.0")
    implementation("com.google.android.material:material:1.1.0")
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation("com.xwray:groupie:2.8.1")
    implementation("com.xwray:groupie-kotlin-android-extensions:2.8.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.8.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.1")

    // Testing
    testImplementation(project(":orbit-2-test"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    testImplementation("org.junit.platform:junit-platform-console:1.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.2")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.mockito:mockito-inline:3.4.6")
    testImplementation("com.appmattus.fixture:fixture:0.9.4")
    testImplementation("androidx.test:core-ktx:1.2.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("com.squareup.retrofit2:retrofit-mock:2.9.0")

    // Debugging
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.4")
    implementation("com.squareup.leakcanary:plumber-android:2.4")

    // Dependency Injection
    implementation("org.koin:koin-androidx-viewmodel:2.1.6")
}
