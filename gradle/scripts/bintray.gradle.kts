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

import com.novoda.gradle.release.PublishExtension
import com.novoda.gradle.release.ReleasePlugin

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.novoda:bintray-release:0.9.1")
    }
}

apply<ReleasePlugin>()

configure<PublishExtension> {
    bintrayUser = System.getenv("BINTRAY_USER") ?: System.getProperty("BINTRAY_USER") ?: "unknown"
    bintrayKey = System.getenv("BINTRAY_KEY") ?: System.getProperty("BINTRAY_KEY") ?: "unknown"

    groupId = "com.babylon.orbit"
    artifactId = project.name
    publishVersion = System.getenv("VERSION_TAG") ?: System.getProperty("VERSION_TAG") ?: "unknown"

    repoName = "maven"
    userOrg = "babylonpartners"
    desc = "Orbit, an MVI framework for Android and Kotlin"
    website = "https://github.com/babylonhealth/orbit-android-mvi"
    setLicences("Apache-2.0")

    dryRun = false
}
