/*
 * Copyright 2023-2025 Mikołaj Leszczyński & Appmattus Limited
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

apply<JacocoPlugin>()

val jacocoTask = tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn(tasks.withType(Test::class))

    val coverageSourceDirs = arrayOf(
        "src/commonMain",
        "src/jvmMain"
    )

    // Include all compiled classes.
    val classFiles = layout.buildDirectory.dir("classes/kotlin/jvm").get().asFile.walkBottomUp().toSet()

    // This helps with test coverage accuracy.
    classDirectories.setFrom(classFiles)
    sourceDirectories.setFrom(files(coverageSourceDirs))

    // The resulting test report in binary format.
    // It serves as the basis for human-readable reports.
    layout.buildDirectory.files("jacoco/jvmTest.exec").let {
        executionData.setFrom(it)
    }

    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }
}

tasks.withType<Test> {
    finalizedBy(jacocoTask)
}

configure<JacocoPluginExtension> {
    toolVersion = "0.8.13"
}
