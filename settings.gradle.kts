/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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

include(
    "orbit-core",
    "orbit-coroutines",
    "orbit-livedata",
    "orbit-rxjava1",
    "orbit-rxjava2",
    "orbit-rxjava3",
    "orbit-test",
    "orbit-viewmodel",
    "samples:orbit-calculator",
    "samples:orbit-posts",
    "samples:orbit-stocklist",
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
