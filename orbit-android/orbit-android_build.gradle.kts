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
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

dependencies {
    implementation(project(":orbit"))

    implementation(kotlin("stdlib-jdk8"))

    implementation(ProjectDependencies.androidxLifecycleComponents)
    implementation(ProjectDependencies.androidxLifecycleSavedState)
    kapt(ProjectDependencies.androidxLifecycleCompiler)

    implementation(ProjectDependencies.rxJava2)
    implementation(ProjectDependencies.rxKotlin)
    implementation(ProjectDependencies.rxAndroid)

    // Testing
    GroupedDependencies.testsImplementation.forEach { testImplementation(it) }
    GroupedDependencies.testsRuntime.forEach { testRuntimeOnly(it) }
    testImplementation(ProjectDependencies.kotlinFixture)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}
