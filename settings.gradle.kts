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

include(
    "orbit-2-core",
    "orbit-2-coroutines",
    "orbit-2-livedata",
    "orbit-2-rxjava1",
    "orbit-2-rxjava2",
    "orbit-2-rxjava3",
    "orbit-2-test",
    "orbit-2-viewmodel",
    "samples:orbit-2-calculator",
    "samples:orbit-2-posts",
    "samples:orbit-2-stocklist",
    "test-common"
)

fun renameBuildFileToModuleName(project: ProjectDescriptor) {
    project.buildFileName = "${project.name}_build.gradle.kts"
    project.children.forEach { child -> renameBuildFileToModuleName(child) }
}
// Will rename every module's build.gradle file to use its name instead of `build`.
// E.g. `app/build.gradle` will become `app/app.gradle`
// The root build.gradle file will remain untouched
rootProject.children.forEach { subproject -> renameBuildFileToModuleName(subproject) }
