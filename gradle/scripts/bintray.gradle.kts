/*
 * Copyright 2021 Mikolaj Leszczynski & Matthew Dolan
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

val projectGroupId = "org.orbit-mvi"
val projectArtifactId = project.name

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
                version = tag

                afterEvaluate {
                    if (project.plugins.hasPlugin("com.android.library")) {
                        artifact(project.tasks.named("sourcesJar").get())
                    }
                }

                pom {
                    groupId = projectGroupId
                    artifactId = projectArtifactId
                    version = tag

                    name.set(project.name)
                    url.set("https://github.com/orbit-mvi/orbit-mvi")
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
        userOrg = "orbitmvi"
        name = projectArtifactId
        desc = "Orbit MVI for Kotlin and Android"
        websiteUrl = "https://github.com/orbit-mvi/orbit-mvi"
        issueTrackerUrl = "https://github.com/orbit-mvi/orbit-mvi/issues"
        vcsUrl = "https://github.com/orbit-mvi/orbit-mvi"
        githubRepo = "orbit-mvi/orbit-mvi"

        setLicenses("Apache-2.0")

        version.apply {
            name = tag
            vcsTag = tag
        }
    }
}
