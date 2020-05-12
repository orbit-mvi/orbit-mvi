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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.novoda.gradle.release.PublishExtension
import com.novoda.gradle.release.ReleasePlugin

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(PluginDependencies.android)
        classpath(PluginDependencies.kotlin)
        classpath("com.novoda:bintray-release:${Versions.novodaBintrayRelease}")
    }
}

plugins {
    id("com.github.ben-manes.versions") version Versions.gradleVersionsPlugin
    id("com.appmattus.markdown") version Versions.markdownLintPlugin
}

apply(from = "gradle/scripts/detekt.gradle.kts")
apply(from = "gradle/scripts/jacoco-combinedreport.gradle.kts")

subprojects {
    repositories {
        google()
        jcenter()
    }

    apply(from = "$rootDir/gradle/scripts/tests.gradle.kts")
}

task("clean", type = Delete::class) {
    delete(rootProject.buildDir)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    resolutionStrategy {
        componentSelection {
            all {
                fun isNonStable(version: String) = listOf(
                    "alpha",
                    "beta",
                    "rc",
                    "cr",
                    "m",
                    "preview",
                    "b",
                    "ea"
                ).any { qualifier ->
                    version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                }
                if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                    reject("Release candidate")
                }
            }
        }
    }
}

subprojects.forEach { project ->
    project.plugins.withId("org.jetbrains.kotlin.jvm") {
        configurePub(project)
    }
    project.plugins.withId("org.jetbrains.kotlin.android") {
        configurePub(project)
    }
}

fun configurePub(project: Project) {
    val tag = (System.getenv("GITHUB_REF") ?: System.getProperty("GITHUB_REF"))
        ?.replaceFirst("refs/tags/", "")

    val split = tag?.split("/")
    val tagName = split?.get(0)
    val tagVersion = split?.get(1)

    val apply = when (tagName) {
        "orbit2" -> project.name.startsWith("orbit-2")
        "orbit" -> project.name.startsWith(tagName) && !project.name.startsWith("orbit-2")
        else -> false
    }

    if (apply) {
        project.apply<ReleasePlugin>()
        project.configure<PublishExtension> {
            bintrayUser = System.getenv("BINTRAY_USER")
                ?: System.getProperty("BINTRAY_USER") ?: "unknown"
            bintrayKey = System.getenv("BINTRAY_KEY") ?: System.getProperty(
                "BINTRAY_KEY"
            ) ?: "unknown"

            groupId = if (tagName == "orbit2") "com.babylon.orbit2" else "com.babylon.orbit"
            artifactId = project.name.replace("2-", "")
            publishVersion = tagVersion

            repoName = "maven"
            userOrg = "babylonpartners"
            desc = "Orbit MVI for Kotlin and Android"
            website = "https://github.com/babylonhealth/orbit-mvi"
            setLicences("Apache-2.0")

            dryRun = false
        }
    }
}
