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

import com.android.build.gradle.LibraryExtension
import com.appmattus.markdown.rules.LineLengthRule
import com.appmattus.markdown.rules.ProperNamesRule
import com.appmattus.markdown.rules.ProperNamesRule.Companion.DefaultNames
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(PluginDependencies.android)
        classpath(PluginDependencies.kotlin)
        classpath(PluginDependencies.safeargs)
    }
}

plugins {
    kotlin(module = "plugin.serialization") version Versions.kotlin
    id("com.github.ben-manes.versions") version Versions.gradleVersionsPlugin
    id("com.appmattus.markdown") version Versions.markdownLintPlugin
}

apply(from = "gradle/scripts/detekt.gradle.kts")

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

tasks.withType<DependencyUpdatesTask> {
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

subprojects {
    repositories {
        google()
        jcenter()
    }

    tasks.withType<Test> {
        @Suppress("UnstableApiUsage")
        useJUnitPlatform {
            includeEngines(
                "junit-jupiter"
            )
        }
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    tasks.withType<KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true
        }
    }
    plugins.withType<JavaBasePlugin> {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
    plugins.withId("com.android.application") {
        apply(from = "$rootDir/gradle/scripts/jacoco-android.gradle.kts")
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
        apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")
        apply(from = "$rootDir/gradle/scripts/bintray.gradle.kts")
    }
    plugins.withId("com.android.library") {
        apply(from = "$rootDir/gradle/scripts/jacoco-android.gradle.kts")
        apply(from = "$rootDir/gradle/scripts/bintray.gradle.kts")

        val sourceSets = extensions.findByType<LibraryExtension>()!!.sourceSets
        tasks.register<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(sourceSets["main"].java.srcDirs)
        }

        configure<LibraryExtension> {
            compileSdkVersion(29)
            defaultConfig {
                minSdkVersion(21)
                targetSdkVersion(29)
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }
        }
    }
}

markdownlint {
    rules {
        +LineLengthRule(codeBlocks = false)
        +ProperNamesRule(names = DefaultNames + "Orbit")
    }
}
