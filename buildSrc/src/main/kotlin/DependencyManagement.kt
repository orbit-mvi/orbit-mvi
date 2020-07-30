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

object Versions {

    const val gradleVersionsPlugin = "0.29.0"
    const val gradleAndroidPlugin = "4.0.1"
    const val markdownLintPlugin = "0.6.0"
    const val detektPlugin = "1.10.0"
    const val bintray = "1.8.5"
    const val safeargs = "2.3.0"

    const val kotlin = "1.3.72"
    const val coroutines = "1.3.8"

    const val androidxLifecycles = "2.2.0"
    const val androidxAnnotation = "1.1.0"
    const val androidxTesting = "2.1.0"
    const val androidxAppCompat = "1.1.0"
    const val androidxConstrainLayout = "1.1.3"
    const val androidMaterial = "1.1.0"
    const val androidxNavigation = "1.0.0"

    const val rxJava2 = "2.2.19"

    const val koin = "2.1.6"
    const val groupie = "2.8.0"

    const val lightstreamer = "4.2.1"

    const val desugar = "1.0.10"

    // Testing
    const val spek = "2.0.12"
    const val junitPlatform = "1.6.2"
    const val assertJ = "3.16.1"
    const val mockitoKotlin = "2.2.0"
    const val mockito = "3.4.4"
    const val junitRuntime = "5.6.2"
    const val kotlinFixture = "0.9.4"
    const val jacoco = "0.8.5"
}

object ProjectDependencies {
    // Kotlin
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val kotlinCoroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val kotlinCoroutinesRx2 =
        "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${Versions.coroutines}"
    const val kotlinTest = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}"

    // AndroidX
    const val androidxAppCompat = "androidx.appcompat:appcompat:${Versions.androidxAppCompat}"
    const val androidxAnnotation = "androidx.annotation:annotation:${Versions.androidxAnnotation}"
    const val androidxConstrainLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.androidxConstrainLayout}"
    const val androidxLifecycleComponents =
        "androidx.lifecycle:lifecycle-extensions:${Versions.androidxLifecycles}"
    const val androidxLifecycleCompiler =
        "androidx.lifecycle:lifecycle-compiler:${Versions.androidxLifecycles}"
    const val androidxLifecycleSavedState =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.androidxLifecycles}"
    const val androidxLifecycleKtx =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidxLifecycles}"
    const val androidMaterial = "com.google.android.material:material:${Versions.androidMaterial}"
    const val androidxNavigationFragmentKtx = "android.arch.navigation:navigation-fragment-ktx:${Versions.androidxNavigation}"
    const val androidxNavigationUiKtx = "android.arch.navigation:navigation-ui-ktx:${Versions.androidxNavigation}"

    // Streaming service
    const val lightstreamer = "com.lightstreamer:ls-android-client:${Versions.lightstreamer}"

    // Dependency injection
    const val koinViewModel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
    const val koinTest = "org.koin:koin-test:${Versions.koin}"

    // UI
    const val groupie = "com.xwray:groupie:${Versions.groupie}"
    const val groupieKotlinAndroidExtensions =
        "com.xwray:groupie-kotlin-android-extensions:${Versions.groupie}"
    const val groupieViewBinding = "com.xwray:groupie-viewbinding:${Versions.groupie}"

    // RxJava
    const val rxJava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxJava2}"

    // Tools
    const val detektFormatting = "io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detektPlugin}"
    const val desugar = "com.android.tools:desugar_jdk_libs:${Versions.desugar}"

    // Test prerequisites
    const val androidXTesting =
        "androidx.arch.core:core-testing:${Versions.androidxTesting}"
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
    const val detekt = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Versions.detektPlugin}"
    const val bintray = "com.jfrog.bintray.gradle:gradle-bintray-plugin:${Versions.bintray}"
    const val safeargs = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.safeargs}"
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
        ProjectDependencies.mockitoInline,
        ProjectDependencies.kotlinFixture,
        ProjectDependencies.assertJ
    )

    val testsRuntime = listOf(
        ProjectDependencies.spekRunner,
        ProjectDependencies.junitJupiterEngine
    )
}
