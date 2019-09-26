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

import io.reactivex.Observable

data class SingleSubscriberUniqueKeyResourceTestCase(
    val id: Int = testCaseId,
    val caching: Boolean,
    val autoLoading: Boolean,
    val executorOutput: ResourceStream<Unit> = Observable.just(ResourceStatus.Ready(Unit)),
    val command: RepositoryCommand,
    val expectedOutput: List<ResourceStatus<Unit>>
)

// Exceptions are always unique, assertions fail if we don't use the same one
private val exampleException: Throwable = IllegalStateException()
private var testCaseId: Int = 1
    get() = field++

fun buildSingleSubscriberUniqueKeyResourceTestCases() = listOf(

    // Happy path test cases ==========================================================================================
    SingleSubscriberUniqueKeyResourceTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    /*  ^
     *  |
     *  5
     */

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),
    // ================================================================================================================

    // Error test cases ===============================================================================================
    SingleSubscriberUniqueKeyResourceTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        executorOutput = Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        executorOutput = Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    /*  ^
     *  |
     *  10
     */

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        executorOutput = Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        executorOutput = Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        executorOutput = Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        executorOutput = Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        executorOutput = Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    /*  ^
     *  |
     *  15
     */

    SingleSubscriberUniqueKeyResourceTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        executorOutput = Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    )
    // ================================================================================================================
)
