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
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(ProjectDependencies.kotlinCoroutines)

    implementation("androidx.test.espresso:espresso-core:3.3.0")
    implementation("androidx.test.espresso:espresso-idling-resource:3.3.0")
    implementation("androidx.test.ext:junit:1.1.2")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.test:core-ktx:1.3.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.1")

    implementation("com.facebook.testing.screenshot:core:0.11.0")
    implementation("com.amazonaws:aws-android-sdk-s3:2.16.11")

    // should be runtimeOnly to not add bloat to projects that don't use these classes
    implementation("com.airbnb.android:lottie:3.4.1")
    implementation("com.google.android.material:material:1.2.1")

    // Testing
    GroupedDependencies.testsImplementation.forEach { testImplementation(it) }
    testRuntimeOnly(ProjectDependencies.junitJupiterEngine)
}
