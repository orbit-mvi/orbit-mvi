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

include(
    "orbit",
    "orbit-android",
    "sampleapp",
    "orbit-state-generator"
)

// Will rename every module's build.gradle file to use its name instead of `build`.
// E.g. `app/build.gradle` will become `app/app_build.gradle`
// The root build.gradle file remains untouched
rootProject.children.forEach { project ->
    project.buildFileName = "${project.name}_build.gradle.kts"
}
