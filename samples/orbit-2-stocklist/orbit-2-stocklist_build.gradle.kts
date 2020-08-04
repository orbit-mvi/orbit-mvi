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
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        applicationId = "com.babylon.orbit2.sample.stocklist"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        coreLibraryDesugaringEnabled = true

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
    jcenter()
    maven { setUrl("https://www.lightstreamer.com/repo/maven") }
    maven { setUrl("https://dl.bintray.com/lisawray/maven") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":orbit-2-core"))
    implementation(project(":orbit-2-livedata"))
    implementation(project(":orbit-2-viewmodel"))
    implementation(project(":orbit-2-coroutines"))

    implementation(ProjectDependencies.kotlinCoroutines)
    implementation(ProjectDependencies.androidxAppCompat)
    implementation(ProjectDependencies.androidxConstraintLayout)
    implementation(ProjectDependencies.androidMaterial)
    implementation(ProjectDependencies.androidxNavigationFragmentKtx)
    implementation(ProjectDependencies.androidxNavigationUiKtx)
    implementation(ProjectDependencies.lightstreamer)
    implementation(ProjectDependencies.groupie)
    implementation(ProjectDependencies.groupieKotlinAndroidExtensions)
    implementation(ProjectDependencies.groupieViewBinding)
    implementation(ProjectDependencies.koinViewModel)
    kapt(ProjectDependencies.androidxLifecycleCompiler)

    // Testing
    testImplementation(project(":orbit-2-test"))
    GroupedDependencies.testsImplementation.forEach { testImplementation(it) }
    testRuntimeOnly(ProjectDependencies.junitJupiterEngine)
    testImplementation(ProjectDependencies.koinTest)

    coreLibraryDesugaring(ProjectDependencies.desugar)
}
