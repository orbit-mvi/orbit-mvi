/*
 * Copyright 2021-2022 Mikołaj Leszczyński & Appmattus Limited
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
 */

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.androidxCompose
    }
}

dependencies {
    api(project(":orbit-core"))

    implementation(ProjectDependencies.androidxComposeRuntime)
    implementation(ProjectDependencies.androidxLifecycleRuntimeKtx)
    implementation(ProjectDependencies.androidxComposeUi)

    // Testing
    implementation(kotlin("test-junit"))
    testImplementation(project(":orbit-test"))
    testImplementation(project(":test-common"))
    testImplementation(ProjectDependencies.androidxEspressoCore)
    testImplementation(ProjectDependencies.robolectric)

    testImplementation(ProjectDependencies.kotlinTest)
    testImplementation(ProjectDependencies.kotlinCoroutinesTest)

    testImplementation(ProjectDependencies.androidxCoreTesting)
    testImplementation(ProjectDependencies.androidxComposeUiTestJunit4)
    testImplementation(ProjectDependencies.androidxComposeUiTestManifest)
}
