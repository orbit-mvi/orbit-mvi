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

import com.android.build.gradle.LibraryExtension
import com.appmattus.markdown.rules.LineLengthRule
import com.appmattus.markdown.rules.ProperNamesRule
import com.appmattus.markdown.rules.ProperNamesRule.Companion.DefaultNames
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost.DEFAULT
import java.net.URL
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(PluginDependencies.android)
        classpath(PluginDependencies.kotlin)
        classpath(PluginDependencies.safeargs)
        classpath(PluginDependencies.atomicfu)
    }
}

plugins {
    kotlin("plugin.serialization") version Versions.kotlin
    id("com.github.ben-manes.versions") version Versions.gradleVersionsPlugin
    id("com.appmattus.markdown") version Versions.markdownLintPlugin
    id("com.vanniktech.maven.publish") version Versions.gradleMavenPublishPlugin apply false
    id("org.jetbrains.dokka") version Versions.dokka
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

// Force Kotlin versions to ensure transitive dependencies don't break our build
allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
            force("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
            // Force Junit version due to security issues with Junit 4.12
            force(ProjectDependencies.junit4)
        }
    }
}

subprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/temporary")
        maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
    }

    version = (System.getenv("GITHUB_REF") ?: System.getProperty("GITHUB_REF"))
        ?.replaceFirst("refs/tags/", "") ?: "unspecified"

    tasks.withType<Test> {
        @Suppress("UnstableApiUsage")
        if (project.name !in listOf("orbit-core", "orbit-test", "orbit-viewmodel")) {
            useJUnitPlatform {
                includeEngines(
                    "junit-jupiter"
                )
            }
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
    plugins.withType<org.jetbrains.dokka.gradle.DokkaPlugin> {
        tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
            dokkaSourceSets {
                configureEach {
                    if (name.startsWith("ios")) {
                        displayName.set("ios")
                    }

                    sourceLink {
                        localDirectory.set(rootDir)
                        remoteUrl.set(URL("https://github.com/orbit-mvi/orbit-mvi/blob/main"))
                        remoteLineSuffix.set("#L")
                    }
                }
            }
        }
    }
    plugins.withId("com.android.application") {
        apply(from = "$rootDir/gradle/scripts/jacoco-android.gradle.kts")
        configure<com.android.build.gradle.AppExtension> {
            sourceSets {
                get("main").java.srcDir("src/main/kotlin")
                get("test").java.srcDir("src/test/kotlin")
            }
        }
    }
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper> {
        apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
            // for strict mode
            explicitApi()
        }
    }
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper> {
        apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
            // for strict mode
            explicitApi()
        }
    }
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper> {
        apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
            // for strict mode
            explicitApi()
        }
    }
    plugins.withId("com.android.library") {
        plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper> {
            configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
                // for strict mode
                explicitApi()
            }
        }
        apply(from = "$rootDir/gradle/scripts/jacoco-android.gradle.kts")

        configure<LibraryExtension> {
            compileSdk = 31
            defaultConfig {
                minSdk = 21
                targetSdk = 31
            }

            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }

            sourceSets {
                get("main").java.srcDir("src/main/kotlin")
                get("test").java.srcDir("src/test/kotlin")
            }
        }
    }
    plugins.withId("com.vanniktech.maven.publish.base") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(DEFAULT, System.getenv("SONATYPE_REPOSITORY_ID"))
        }
    }
}

markdownlint {
    excludes = listOf(
        ".*/build/.*",
        ".*/.docusaurus/.*",
        ".*/node_modules/.*",
        ".*/website/.*" // This is temporary until markdownlint can ignore frontmatter
    )
    rules {
        +LineLengthRule(codeBlocks = false, tables = false)
        +ProperNamesRule(names = DefaultNames + "Orbit")
    }
}

val copyDokkaToWebsite by tasks.registering(Copy::class) {
    dependsOn("dokkaHtmlMultiModule")
    from(files("build/dokka/htmlMultiModule"))
    into(file("website/static/dokka"))
}
