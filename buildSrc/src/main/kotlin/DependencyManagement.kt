/*
 * Copyright 2021-2022 Mikołaj Leszczyński & Appmattus Limited
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

object Versions {

    const val gradleVersionsPlugin = "0.42.0"
    const val gradleAndroidPlugin = "7.2.2"
    const val gradleMavenPublishPlugin = "0.18.0"
    const val markdownLintPlugin = "0.6.0"
    const val detektPlugin = "1.21.0"
    const val safeargs = "2.5.1"
    const val atomicfu = "0.18.5"
    const val dokka = "1.6.10"

    const val kotlin = "1.7.20"
}

object PluginDependencies {
    const val android = "com.android.tools.build:gradle:${Versions.gradleAndroidPlugin}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val detekt = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Versions.detektPlugin}"
    const val safeargs = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.safeargs}"
    const val atomicfu = "org.jetbrains.kotlinx:atomicfu-gradle-plugin:${Versions.atomicfu}"
}
