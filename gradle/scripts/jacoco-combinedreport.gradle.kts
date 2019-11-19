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

apply<JacocoPlugin>()

val jacocoTestReport by tasks.registering(JacocoReport::class) {
    group = "Coverage reports"
    description = "Generates an aggregate report from all subprojects"

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }

    setOnlyIf {
        true
    }
}

tasks.register("check") {
    dependsOn(jacocoTestReport)
}

subprojects {
    plugins.withType<JacocoPlugin> {
        // this is executed for each project that has Jacoco plugin applied
        the<JacocoPluginExtension>().toolVersion = "0.8.5"

        tasks.withType<JacocoReport> {
            val task = this
            if (task.name != "jacocoTestReleaseUnitTestReport") {
                rootProject.tasks.getByName("jacocoTestReport").dependsOn(task)

                jacocoTestReport {
                    executionData.from(task.executionData)
                    additionalSourceDirs.from(task.sourceDirectories)
                    additionalClassDirs.from(task.classDirectories)
                }
            }

            reports {
                html.isEnabled = false
                xml.isEnabled = false
                csv.isEnabled = false
            }
        }

        tasks.withType<Test> {
            rootProject.tasks.getByName("jacocoTestReport").dependsOn(this)
        }
    }
}
