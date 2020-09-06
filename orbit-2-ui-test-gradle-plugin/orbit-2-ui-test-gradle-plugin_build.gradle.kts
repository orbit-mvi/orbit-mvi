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
    `kotlin-dsl`
    //id("org.gradle.kotlin.kotlin-dsl")
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
    }
}

dependencies {
    implementation("org.eclipse.mylyn.github:org.eclipse.egit.github.core:2.1.5")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.1")

    // Testing
    testImplementation(ProjectDependencies.junitPlatformConsole)
    testImplementation(ProjectDependencies.junitJupiterApi)
    testImplementation(ProjectDependencies.junitJupiterParams)
    testImplementation(ProjectDependencies.assertJ)
    testRuntimeOnly(ProjectDependencies.junitJupiterEngine)
}

gradlePlugin {
    plugins {
        create("uitest") {
            id = "com.babylon.orbit2.uitest.gradle"
            implementationClass = "com.babylon.orbit2.uitest.gradle.UITestPlugin"
        }
    }
}

/*validatePlugins {
    failOnWarning = true
    enableStricterValidation = true
}*/

/*tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}*/
