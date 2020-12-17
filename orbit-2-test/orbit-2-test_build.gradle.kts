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
    kotlin("multiplatform")
}
apply<kotlinx.atomicfu.plugin.gradle.AtomicFUGradlePlugin>()

kotlin {
    jvm()
    ios()
    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlin("test-common"))
                implementation(ProjectDependencies.kotlinCoroutines)
                implementation(project(":test-common"))

                api(project(":orbit-2-core"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(ProjectDependencies.kotestAssertions)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(ProjectDependencies.kotlinCoroutines)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(ProjectDependencies.kotlinCoroutines)
            }
        }

        val jvmTest by getting {
            dependencies {

            }
        }

        val iosTest by getting {
            dependencies {

            }
        }
    }
}

//// Fix lack of source code when publishing pure Kotlin projects
//// See https://github.com/novoda/bintray-release/issues/262
//tasks.whenTaskAdded {
//    if (name == "generateSourcesJarForMavenPublication") {
//        this as Jar
//        from(sourceSets.main.get().allSource)
//    }
//}
