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

import kotlinx.atomicfu.plugin.gradle.AtomicFUGradlePlugin

plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}
apply<AtomicFUGradlePlugin>()

kotlin {
    jvm()
    ios()
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("org.orbitmvi.orbit.annotation.OrbitInternal")
        }
        commonMain {
            dependencies {
                implementation(ProjectDependencies.kotlinCoroutines)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(project(":test-common"))
                implementation(project(":orbit-test"))
                implementation(ProjectDependencies.kotlinCoroutines)
            }
        }

        @Suppress("UnusedPrivateMember")
        val jvmMain by getting {
        }

        @Suppress("UnusedPrivateMember")
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        @Suppress("UnusedPrivateMember")
        val iosMain by getting {
        }

        @Suppress("UnusedPrivateMember")
        val iosTest by getting {
        }
    }
}
