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

package com.babylon.orbit.domain.datalayer

data class LoadingInProgressTestCase(
    val id: Int = testCaseId,
    val autoLoading: Boolean,
    val command: RepositoryCommand,
    val expectedOutput: List<ResourceStatus<Unit>>
)

private var testCaseId: Int = 1
    get() = field++

fun buildLoadingInProgressTestCases() = listOf(

    // Happy path / loading test cases ================================================================================
    LoadingInProgressTestCase(
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        expectedOutput = emptyList()
    ),

    LoadingInProgressTestCase(
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null)
        )
    ),

    LoadingInProgressTestCase(
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        expectedOutput = emptyList()
    ),

    LoadingInProgressTestCase(
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null)
        )
    )
)
