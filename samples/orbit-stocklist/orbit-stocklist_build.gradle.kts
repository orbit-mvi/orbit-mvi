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

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "org.orbitmvi.orbit.sample.stocklist"
    compileSdk = 37
    defaultConfig {
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        applicationId = "org.orbitmvi.orbit.sample.stocklist"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        dataBinding = true
    }

    testOptions.unitTests.isIncludeAndroidResources = true

    packaging {
        resources {
            pickFirsts += "build.number"
            pickFirsts += "version.number"
            pickFirsts += "compatibility_version.number"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

repositories {
    mavenCentral()
    maven { setUrl("https://www.lightstreamer.com/repo/maven") }
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

    implementation(libs.androidxConstraintlayout)
    implementation(libs.googleMaterial)
    implementation(libs.androidxNavigationFragmentKtx)
    implementation(libs.androidxNavigationUiKtx)
    implementation(libs.lsAndroidClient)
    implementation(libs.groupie)
    implementation(libs.groupieKotlinAndroidExtensions) {
        // Exclude Parcelable as we use version added by plugin above
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation(libs.groupieViewbinding)
    implementation(libs.koinAndroid)
    implementation(libs.androidxLifecycleCommonJava8)
    implementation(libs.androidxLifecycleLivedata)
    implementation(libs.androidxLifecycleRuntimeKtx)

    coreLibraryDesugaring(libs.desugar)
}
