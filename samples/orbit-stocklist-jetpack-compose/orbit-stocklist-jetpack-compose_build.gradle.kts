/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.orbitmvi.orbit.sample.stocklist.compose"
    compileSdk = 35
    defaultConfig {
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        applicationId = "org.orbitmvi.orbit.sample.stocklist.compose"
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
        // Enables Jetpack Compose for this module
        compose = true
    }

    testOptions.unitTests.isIncludeAndroidResources = true

    packaging {
        resources {
            excludes.addAll(
                listOf(
                    "META-INF/INDEX.LIST",
                    "META-INF/io.netty.versions.properties"
                )
            )
        }
    }

    sourceSets {
        get("main").java.srcDir("src/main/kotlin")
        get("test").java.srcDir("src/test/kotlin")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":orbit-core"))
    implementation(project(":orbit-viewmodel"))
    implementation(project(":orbit-compose"))

    implementation(libs.androidxNavigationFragmentKtx)
    implementation(libs.androidxNavigationUiKtx)
    implementation(libs.lsAndroidClient)
    implementation(libs.androidxLifecycleCommonJava8)
    implementation(libs.androidxLifecycleRuntimeKtx)

    // Dependency Injection
    implementation(libs.hiltAndroid)
    kapt(libs.hiltAndroidCompiler)

    coreLibraryDesugaring(libs.desugar)

    // Jetpack Compose
    implementation(libs.androidxActivityCompose)
    implementation(libs.androidxComposeUi)
    // Tooling support (Previews, etc.)
    implementation(libs.androidxComposeUiTooling)
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation(libs.androidxComposeFoundation)
    // Material Design
    implementation(libs.androidxComposeMaterial)
    // Material design icons
    implementation(libs.androidxComposeMaterialIconsCore)
    // Navigation
    implementation(libs.androidxNavigationCompose)
    implementation(libs.androidxHiltNavigationCompose)
    // Lifecycle
    implementation(libs.androidxLifecycleViewmodelCompose)
}
