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

    const val gradleVersionsPlugin = "0.36.0"
    const val gradleAndroidPlugin = "4.1.1"
    const val markdownLintPlugin = "0.6.0"
    const val detektPlugin = "1.14.1"
    const val bintray = "1.8.5"
    const val safeargs = "2.3.2"
    const val atomicfu = "0.14.4"

    const val kotlin = "1.4.21"
    const val coroutines = "1.4.2"

    const val androidxLifecycles = "2.2.0"
    const val androidxAnnotation = "1.1.0"
    const val androidxTesting = "2.1.0"
    const val androidxEspresso = "3.3.0"

    const val rxJava1 = "1.3.8"
    const val rxJava2 = "2.2.20"
    const val rxJava3 = "3.0.8"

    const val desugar = "1.0.10"

    // Testing
    const val junitPlatform = "1.7.0"
    const val assertJ = "3.18.1"
    const val mockitoKotlin = "2.2.0"
    const val mockito = "3.6.28"
    const val junitRuntime = "5.7.0"
    const val jacoco = "0.8.5"
    const val robolectric = "4.4"
}

object ProjectDependencies {
    // Kotlin
    const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val kotlinCoroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    const val kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    const val kotlinCoroutinesRx2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:${Versions.coroutines}"
    const val kotlinCoroutinesRx3 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx3:${Versions.coroutines}"
    const val kotlinTest = "org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}"

    // AndroidX
    const val androidxAnnotation = "androidx.annotation:annotation:${Versions.androidxAnnotation}"
    const val androidxLifecycleComponents = "androidx.lifecycle:lifecycle-extensions:${Versions.androidxLifecycles}"
    const val androidxLifecycleSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.androidxLifecycles}"
    const val androidxLifecycleKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.androidxLifecycles}"
    const val androidxLiveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.androidxLifecycles}"
    const val androidxEspressoIdlingResource = "androidx.test.espresso:espresso-idling-resource:${Versions.androidxEspresso}"
    const val androidxEspressoCore = "androidx.test.espresso:espresso-core:${Versions.androidxEspresso}"

    // RxJava
    const val rxJava1 = "io.reactivex:rxjava:${Versions.rxJava1}"
    const val rxJava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxJava2}"
    const val rxJava3 = "io.reactivex.rxjava3:rxjava:${Versions.rxJava3}"

    // Tools
    const val detektFormatting = "io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detektPlugin}"

    // Test prerequisites
    const val androidxTesting = "androidx.arch.core:core-testing:${Versions.androidxTesting}"
    const val junitPlatformConsole = "org.junit.platform:junit-platform-console:${Versions.junitPlatform}"
    const val assertJ = "org.assertj:assertj-core:${Versions.assertJ}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    const val mockitoInline = "org.mockito:mockito-inline:${Versions.mockito}"
    const val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junitRuntime}"
    const val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Versions.junitRuntime}"
    const val junitJupiterParams = "org.junit.jupiter:junit-jupiter-params:${Versions.junitRuntime}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
}

object PluginDependencies {
    const val android = "com.android.tools.build:gradle:${Versions.gradleAndroidPlugin}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val detekt = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Versions.detektPlugin}"
    const val bintray = "com.jfrog.bintray.gradle:gradle-bintray-plugin:${Versions.bintray}"
    const val safeargs = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.safeargs}"
    const val atomicfu = "org.jetbrains.kotlinx:atomicfu-gradle-plugin:${Versions.atomicfu}"
}

object GroupedDependencies {
    val testsImplementation = listOf(
        ProjectDependencies.junitPlatformConsole,
        ProjectDependencies.junitJupiterApi,
        ProjectDependencies.junitJupiterParams,
        ProjectDependencies.assertJ
    )
}
