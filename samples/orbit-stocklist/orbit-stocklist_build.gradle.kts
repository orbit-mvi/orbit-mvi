/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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
    compileSdk = 31
    defaultConfig {
        minSdk = 23
        targetSdk = 31
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

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        dataBinding = true
    }

    testOptions.unitTests.isIncludeAndroidResources = true

    packagingOptions {
        pickFirst("build.number")
        pickFirst("version.number")
        pickFirst("compatibility_version.number")
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/io.netty.versions.properties")
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

    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")
    implementation("com.lightstreamer:ls-android-client:4.2.5")
    implementation("com.github.lisawray.groupie:groupie:2.10.0")
    implementation("com.github.lisawray.groupie:groupie-kotlin-android-extensions:2.10.0")
    implementation("com.github.lisawray.groupie:groupie-viewbinding:2.10.0")
    implementation("io.insert-koin:koin-android:3.1.4")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${Versions.desugar}")
}
