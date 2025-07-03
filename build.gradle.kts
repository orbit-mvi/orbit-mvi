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

import com.android.build.gradle.LibraryExtension
import com.appmattus.markdown.rules.LineLengthRule
import com.appmattus.markdown.rules.ProperNamesRule
import com.appmattus.markdown.rules.ProperNamesRule.Companion.DefaultNames
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.ZonedDateTime

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.buildscript.android)
        classpath(libs.buildscript.kotlin)
        classpath(libs.buildscript.safeargs)
        classpath(libs.buildscript.hilt)
    }
}

plugins {
    alias(libs.plugins.kotlin.pluginSerialization)
    alias(libs.plugins.gradleVersionsPlugin)
    alias(libs.plugins.markdownlintGradlePlugin)
    alias(libs.plugins.gradleMavenPublishPlugin) apply false
    alias(libs.plugins.dokkaPlugin)
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.binaryCompatibilityValidator)
}

apply(from = "gradle/scripts/detekt.gradle.kts")

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
    plugins.withType<org.jetbrains.dokka.gradle.DokkaPlugin> {
        dokka {
            dokkaSourceSets {
                configureEach {
                    if (name.startsWith("ios")) {
                        displayName.set("ios")
                    }

                    sourceLink {
                        localDirectory.set(rootDir)
                        remoteUrl("https://github.com/orbit-mvi/orbit-mvi/blob/main")
                        remoteLineSuffix.set("#L")
                    }
                }
            }

            pluginsConfiguration.html {
                customAssets.from("$rootDir/dokka/logo-icon.svg")
                footerMessage.set(
                    provider {
                        "Copyright © 2021-${ZonedDateTime.now().year} Mikołaj Leszczyński & Appmattus Limited"
                    }
                )
            }
        }
    }
}

subprojects {
    repositories {
        google()
        mavenCentral()
    }

    version = (System.getenv("GITHUB_REF") ?: System.getProperty("GITHUB_REF"))
        ?.replaceFirst("refs/tags/", "") ?: "unspecified"

    tasks.withType<Test> {
        if (project.name !in listOf("orbit-core", "orbit-test", "orbit-viewmodel", "orbit-compose")) {
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
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
            if (project.name !in listOf("orbit-stocklist-jetpack-compose")) {
                allWarningsAsErrors = true
            }
        }
    }
    plugins.withType<JavaBasePlugin> {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
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
        if (project.name !in listOf("orbit-viewmodel", "orbit-compose", "composeApp")) {
            apply(from = "$rootDir/gradle/scripts/jacoco.gradle.kts")
        }
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
            compileSdk = 35
            defaultConfig {
                minSdk = 21
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

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
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
    dependsOn("dokkaGenerate")
    from(files("build/dokka/html"))
    into(file("website/static/dokka"))
}

dependencies {
    dokka(project(":orbit-core:"))
    dokka(project(":orbit-compose:"))
    dokka(project(":orbit-viewmodel:"))
    dokka(project(":orbit-test:"))
}

fun isMainModule(project: Project): Boolean {
    return project.name.startsWith("orbit-") && !project.path.contains(":samples")
}

fun isSampleModule(project: Project): Boolean {
    val exclusions = listOf(
        "orbit-posts-compose-multiplatform",
    )
    return project.path.contains(":samples:") && !exclusions.contains(project.name)
}

val checkMainModules by tasks.registering {
    dependsOn(subprojects.filter { isMainModule(it) }.map { "${it.path}:check" })
}

val assembleMainModules by tasks.registering {
    dependsOn(subprojects.filter { isMainModule(it) }.map { "${it.path}:assemble" })
}

val checkSamples by tasks.registering {
    dependsOn(subprojects.filter { isSampleModule(it) }.map { "${it.path}:check" })
}

apiValidation {
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
    }
    ignoredPackages.add("org.orbitmvi.orbit.internal")
    ignoredProjects.addAll(
        listOf(
            "test-common",
            "samples",
            "orbit-calculator",
            "orbit-posts",
            "composeApp",
            "orbit-stocklist",
            "orbit-stocklist-jetpack-compose",
            "orbit-text",
        )
    )
    nonPublicMarkers.add("org.orbitmvi.orbit.annotation.OrbitInternal")
    validationDisabled = false
    apiDumpDirectory = "abi/validation"
}
