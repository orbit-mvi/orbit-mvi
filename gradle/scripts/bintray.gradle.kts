/*
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
 */

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin

buildscript {
    repositories {
        google()
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath(PluginDependencies.bintray)
    }
}

repositories {
    google()
    jcenter()
}

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
    val projectGroupId = if (tagName == "orbit2") "com.babylon.orbit2" else "com.babylon.orbit"
    val projectArtifactId = project.name.replace("2-", "")

    apply<BintrayPlugin>()
    apply<MavenPublishPlugin>()

    if (project.plugins.hasPlugin("java")) {
        configure<JavaPluginExtension> {
            withSourcesJar()
        }
    }

    afterEvaluate {
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("release") {
                    if (project.plugins.hasPlugin("com.android.library")) {
                        from(components["release"])
                    } else {
                        from(components["java"])
                    }

                    groupId = projectGroupId
                    artifactId = projectArtifactId
                    version = tagVersion

                    afterEvaluate {
                        if (project.plugins.hasPlugin("com.android.library")) {
                            artifact(project.tasks.named("sourcesJar").get())
                        }
                    }

                    pom {
                        groupId = projectGroupId
                        artifactId = projectArtifactId
                        version = tagVersion

                        name.set(project.name)
                        url.set("https://github.com/babylonhealth/orbit-mvi")
                    }
                }
            }
        }
    }

    configure<BintrayExtension> {
        user = System.getenv("BINTRAY_USER") ?: System.getProperty("BINTRAY_USER") ?: "unknown"
        key = System.getenv("BINTRAY_KEY") ?: System.getProperty("BINTRAY_KEY") ?: "unknown"
        publish = true
        dryRun = false
        override = false

        setPublications("release")

        pkg.apply {
            repo = "maven"
            userOrg = "babylonpartners"
            name = projectArtifactId
            desc = "Orbit MVI for Kotlin and Android"
            websiteUrl = "https://github.com/babylonhealth/orbit-mvi"
            issueTrackerUrl = "https://github.com/babylonhealth/orbit-mvi/issues"
            vcsUrl = "https://github.com/appmattus/babylonhealth/orbit-mvi"
            githubRepo = "babylonhealth/orbit-mvi"

            setLicenses("Apache-2.0")

            version.apply {
                name = tagName
                vcsTag = tagName
            }
        }
    }
}
