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

    const val gradleVersionsPlugin = "0.23.0"
    const val gradleAndroidPlugin = "3.5.0"
    const val markdownLintPlugin = "0.5.0"

    const val kotlin = "1.3.50"

    const val androidLifecycles = "2.0.0"
    const val androidSupportLibrary = "1.0.0"
    const val androidSupportLibraryConstrainLayout = "1.1.3"
    const val androidMaterial = "1.1.0-alpha09"
    const val androidKoin = "2.0.1"
    const val androidRxBindings = "3.0.0"

    const val javaxInject = "1"

    const val rxJava2 = "2.2.12"
    const val rxJava2Extensions = "0.20.10"
    const val rxRelay = "2.1.1"
    const val rxKotlin = "2.4.0"
    const val rxAndroid = "2.1.1"
    const val autodispose = "1.3.0"

    const val timber = "4.7.1"

    // Testing
    const val spek = "1.2.1"
    const val junitPlatform = "1.5.1"
    const val assertk = "0.19"
    const val mockitoKotlin = "2.1.0"
}

object ProjectDependencies {
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"

    // Android libraries
    const val androidLifecycleComponents = "androidx.lifecycle:lifecycle-extensions:${Versions.androidLifecycles}"
    const val androidLifecycleCompiler = "androidx.lifecycle:lifecycle-compiler:${Versions.androidLifecycles}"
    const val androidSupportLibrary = "androidx.appcompat:appcompat:${Versions.androidSupportLibrary}"
    const val androidSupportLibraryConstrainLayout = "androidx.constraintlayout:constraintlayout:" +
            Versions.androidSupportLibraryConstrainLayout
    const val androidMaterial = "com.google.android.material:material:${Versions.androidMaterial}"
    const val androidKoinViewModel = "org.koin:koin-androidx-viewmodel:${Versions.androidKoin}"
    const val androidRxBindings = "com.jakewharton.rxbinding3:rxbinding-material:${Versions.androidRxBindings}"

    // Dagger dependency injection framework.
    // See https://google.github.io/dagger/ for more details.
    const val javaxInject = "javax.inject:javax.inject:${Versions.javaxInject}"

    // Reactive extension related stuff
    const val rxJava2 = "io.reactivex.rxjava2:rxjava:${Versions.rxJava2}"
    const val rxJava2Extensions = "com.github.akarnokd:rxjava2-extensions:${Versions.rxJava2Extensions}"
    const val rxRelay = "com.jakewharton.rxrelay2:rxrelay:${Versions.rxRelay}"
    const val rxKotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxKotlin}"
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"
    const val autodispose = "com.uber.autodispose:autodispose:${Versions.autodispose}"
    const val autodisposeArchComponents = "com.uber.autodispose:autodispose-android-archcomponents:${Versions.autodispose}"

    // Debugging tools
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    // Spek test prerequisites
    const val spekApi = "org.jetbrains.spek:spek-api:${Versions.spek}"
    const val spekJunitPlatformEngine = "org.jetbrains.spek:spek-junit-platform-engine:${Versions.spek}"
    const val junitPlatformConsole = "org.junit.platform:junit-platform-console:${Versions.junitPlatform}"
    const val assertk = "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}"
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
}

object PluginDependencies {
    const val android = "com.android.tools.build:gradle:${Versions.gradleAndroidPlugin}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}

object GroupedDependencies {
    val spekTestsImplementation = listOf(
        ProjectDependencies.kotlinReflect,
        ProjectDependencies.spekApi,
        ProjectDependencies.assertk,
        ProjectDependencies.mockitoKotlin
    )

    val spekTestsRuntime = listOf(
        ProjectDependencies.junitPlatformConsole,
        ProjectDependencies.spekJunitPlatformEngine
    )
}
