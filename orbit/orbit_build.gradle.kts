import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

plugins {
    id("java-library")
    kotlin("jvm")
}

apply(from = "$rootDir/gradle/scripts/bintray.gradle.kts")

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(ProjectDependencies.rxJava2)
    implementation(ProjectDependencies.rxJava2Extensions)
    implementation(ProjectDependencies.rxRelay)
    implementation(ProjectDependencies.rxKotlin)
    implementation(ProjectDependencies.javaxInject)

    // Testing
    GroupedDependencies.spekTestsImplementation.forEach { testImplementation(it) }
    GroupedDependencies.spekTestsRuntime.forEach { testRuntimeOnly(it) }
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines = setOf("spek2")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.getByName("check").dependsOn(rootProject.tasks.getByName("detekt"))
tasks.getByName("check").dependsOn(rootProject.tasks.getByName("markdownlint"))

// Fix lack of source code when publishing pure Kotlin projects
// See https://github.com/novoda/bintray-release/issues/262
tasks.whenTaskAdded {
    if (name == "generateSourcesJarForMavenPublication") {
        this as Jar
        from(sourceSets.main.get().allSource)
    }
}
