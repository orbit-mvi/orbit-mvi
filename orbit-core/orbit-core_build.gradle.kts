/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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
    id(libs.plugins.gradleMavenPublishPlugin.get().pluginId)
    id(libs.plugins.dokkaPlugin.get().pluginId)
}

kotlin {
    jvm()

    js {
        browser {
            testTask {
                useMocha {
                    timeout = "10s"
                }
            }
        }

        nodejs {
            testTask {
                useMocha {
                    timeout = "10s"
                }
            }
        }
    }

    wasmJs {
        browser()
    }

    // Tier 1
    // Apple macOS hosts only:
    macosX64() // Running tests
    macosArm64() // Running tests
    iosSimulatorArm64() // Running tests
    iosX64() // Running tests

    // Tier 2
    linuxX64() // Running tests
    linuxArm64()
    // Apple macOS hosts only:
    watchosSimulatorArm64() // Running tests
    watchosX64() // Running tests
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64() // Running tests
    tvosX64() // Running tests
    tvosArm64()
    iosArm64()

    // Tier 3
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    mingwX64() // Running tests
    // Apple macOS hosts only:
    watchosDeviceArm64()

    // Apply the default hierarchy again. It'll create, for example, the iosMain source set:
    applyDefaultHierarchyTemplate()

    sourceSets {
        val jvmAndNativeMain by creating {
            dependsOn(commonMain.get())
        }
        val jvmAndNativeTest by creating {
            dependsOn(commonTest.get())
        }

        nativeMain.get().dependsOn(jvmAndNativeMain)
        jvmMain.get().dependsOn(jvmAndNativeMain)
        nativeTest.get().dependsOn(jvmAndNativeTest)
        jvmTest.get().dependsOn(jvmAndNativeTest)

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("org.orbitmvi.orbit.annotation.OrbitInternal")
            languageSettings.optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
        }

        commonMain.dependencies {
            api(libs.kotlinCoroutines)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(project(":test-common"))
            implementation(project(":orbit-test"))
            implementation(libs.kotlinCoroutines)
            implementation(libs.kotlinCoroutinesTest)
            implementation(libs.turbine)
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
        }

        jsMain.dependencies {
            implementation(kotlin("test-js"))
        }
    }
}
