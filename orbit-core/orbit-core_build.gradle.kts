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
    kotlin("multiplatform")
}
apply<kotlinx.atomicfu.plugin.gradle.AtomicFUGradlePlugin>()

kotlin {
    jvm()
    ios()
    sourceSets {
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

        val jvmMain by getting {
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val iosMain by getting {
        }

        val iosTest by getting {
        }
    }
}

// // Fix lack of source code when publishing pure Kotlin projects
// // See https://github.com/novoda/bintray-release/issues/262
// tasks.whenTaskAdded {
//    if (name == "generateSourcesJarForMavenPublication") {
//        this as Jar
//        from(sourceSets.main.get().allSource)
//    }
// }
