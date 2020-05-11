/*
 * Copyright 2019 Babylon Partners Limited
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

object Versions {

    const val gradleVersionsPlugin = "0.28.0"
    const val gradleAndroidPlugin = "3.6.3"
    const val markdownLintPlugin = "0.6.0"
    const val jacocoAndroidPlugin = "0.1.4"
    const val detektPlugin = "1.4.0"
    const val novodaBintrayRelease = "0.9.2"

    const val kotlin = "1.3.72"
    const val coroutines = "1.3.5"
    const val coroutineExtensions = "0.0.4"

    const val androidLifecycles = "2.2.0"
    const val androidLifecyclesSavedState = "2.2.0"
    const val androidXTesting = "2.1.0"
    const val androidAppCompat = "1.1.0"
    const val androidConstrainLayout = "1.1.3"
    const val androidMaterial = "1.1.0-rc02"
    const val androidRxBindings = "3.1.0"

    const val javaxInject = "1"

    const val rxJava2 = "2.2.19"
    const val rxJava2Extensions = "0.20.10"
    const val rxKotlin = "2.4.0"
    const val rxAndroid = "2.1.1"

    const val koin = "2.1.5"
    const val groupie = "2.8.0"

    // Testing
    const val spek = "2.0.10"
    const val junitPlatform = "1.6.2"
    const val assertJ = "3.16.1"
    const val mockitoKotlin = "2.2.0"
    const val mockito = "2.23.0"
    const val junitRuntime = "5.6.2"
    const val kotlinFixture = "0.8.2"
}

object ProjectDependencies {
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val kotlinCoroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val kotlinCoroutinesRx2 =
        "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${Versions.coroutines}"
    const val kotlinCoroutineExtensions =
        "com.github.akarnokd:kotlin-flow-extensions:${Versions.coroutineExtensions}"
    const val kotlinTest = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}"

    // Android libraries
    const val androidLifecycleComponents =
        "androidx.lifecycle:lifecycle-extensions:${Versions.androidLifecycles}"
    const val androidLifecycleCompiler =
        "androidx.lifecycle:lifecycle-compiler:${Versions.androidLifecycles}"
    const val androidLifecycleSavedState =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.androidLifecyclesSavedState}"
    const val androidAppCompat = "androidx.appcompat:appcompat:${Versions.androidAppCompat}"
    const val androidConstrainLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.androidConstrainLayout}"
    const val androidMaterial = "com.google.android.material:material:${Versions.androidMaterial}"
    const val androidRxBindings =
        "com.jakewharton.rxbinding3:rxbinding-material:${Versions.androidRxBindings}"

    const val groupie = "com.xwray:groupie:${Versions.groupie}"
    const val groupieKotlinAndroidExtensions =
        "com.xwray:groupie-kotlin-android-extensions:${Versions.groupie}"

    const val koinViewModel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
    const val koinTest = "org.koin:koin-test:${Versions.koin}"

    // Dagger dependency injection framework.
    // See https://google.github.io/dagger/ for more details.
    const val javaxInject = "javax.inject:javax.inject:${Versions.javaxInject}"

    // RxJava related libraries
    const val rxJava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxJava2}"
    const val rxJava2Extensions =
        "com.github.akarnokd:rxjava2-extensions:${Versions.rxJava2Extensions}"
    const val rxKotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxKotlin}"
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"

    // Tools
    const val detektFormatting =
        "io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detektPlugin}"

    // Test prerequisites
    const val androidXTesting =
        "androidx.arch.core:core-testing:${Versions.androidXTesting}"
    const val spekDsl = "org.spekframework.spek2:spek-dsl-jvm:${Versions.spek}"
    const val spekRunner = "org.spekframework.spek2:spek-runner-junit5:${Versions.spek}"
    const val junitPlatformConsole =
        "org.junit.platform:junit-platform-console:${Versions.junitPlatform}"
    const val assertJ = "org.assertj:assertj-core:${Versions.assertJ}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    const val mockitoInline = "org.mockito:mockito-inline:${Versions.mockito}"
    const val kotlinFixture = "com.appmattus.fixture:fixture:${Versions.kotlinFixture}"
    const val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junitRuntime}"
    const val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junitRuntime}"
    const val junitJupiterParams = "org.junit.jupiter:junit-jupiter-params:${Versions.junitRuntime}"
}

object PluginDependencies {
    const val android = "com.android.tools.build:gradle:${Versions.gradleAndroidPlugin}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val jacocoAndroid = "com.dicedmelon.gradle:jacoco-android:${Versions.jacocoAndroidPlugin}"
    const val detekt = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Versions.detektPlugin}"
    const val novodaBintray = "com.novoda:bintray-release:${Versions.novodaBintrayRelease}"
}

object GroupedDependencies {
    val testsImplementation = listOf(
        ProjectDependencies.junitPlatformConsole,
        ProjectDependencies.junitJupiterApi,
        ProjectDependencies.junitJupiterParams,
        ProjectDependencies.spekDsl,
        ProjectDependencies.kotlinReflect,
        ProjectDependencies.assertJ,
        ProjectDependencies.mockitoKotlin,
        ProjectDependencies.mockitoInline,
        ProjectDependencies.kotlinFixture
    )
    val testsImplementationJUnit5 = listOf(
        ProjectDependencies.junitPlatformConsole,
        ProjectDependencies.junitJupiterApi,
        ProjectDependencies.junitJupiterParams,
        ProjectDependencies.mockitoKotlin,
        ProjectDependencies.mockitoInline
    )

    val testsRuntime = listOf(
        ProjectDependencies.spekRunner,
        ProjectDependencies.junitJupiterEngine
    )
}
