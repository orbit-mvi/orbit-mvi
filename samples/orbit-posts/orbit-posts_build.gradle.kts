/*
 * Copyright 2021-2024 Mikołaj Leszczyński & Appmattus Limited
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
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "org.orbitmvi.orbit.sample.posts"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    compileSdk = 34
    defaultConfig {
        minSdk = 21
        targetSdk = 34
        applicationId = "org.orbitmvi.orbit.sample.posts"
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
    }
    lint {
        lintConfig = file("$projectDir/lint.xml")
    }
}

repositories {
    exclusiveContent {
        forRepository {
            maven { setUrl("https://jitpack.io") }
        }
        filter {
            includeGroup("com.github.lisawray.groupie")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":orbit-core"))
    implementation(project(":orbit-viewmodel"))

    // UI
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.lisawray.groupie:groupie:2.10.1")
    implementation("com.github.lisawray.groupie:groupie-viewbinding:2.10.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.11.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    // reflect set to override version from jackson
    implementation(kotlin("reflect"))

    // Dependency Injection
    implementation("io.insert-koin:koin-android:3.5.6")

    // Testing
    testImplementation(project(":orbit-test"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.0")
    testImplementation("org.junit.platform:junit-platform-console:1.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("com.appmattus.fixture:fixture:1.2.0")
    testImplementation("com.squareup.retrofit2:retrofit-mock:2.11.0")
}
