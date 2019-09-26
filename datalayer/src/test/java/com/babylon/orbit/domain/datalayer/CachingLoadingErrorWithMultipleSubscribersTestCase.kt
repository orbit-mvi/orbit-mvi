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

data class CachingLoadingErrorWithMultipleSubscribersTestCase(
    val id: Int = testCaseId,
    val caching: Boolean,
    val autoLoading: Boolean,
    val executorOutput: List<ResourceStream<Unit>> =
        listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit))
        ),
    val command: RepositoryCommand,
    val expectedOutput: List<ResourceStatus<Unit>>,
    val expectedOutput2: List<ResourceStatus<Unit>>
)

// Exceptions are always unique, assertions fail if we don't use the same one
private val exampleException: Throwable = IllegalStateException()
private var testCaseId: Int = 1
    get() = field++

fun buildCachingLoadingErrorWithMultipleSubscribersTestCases() = listOf(

    // Happy path / loading test cases ================================================================================
    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    /*  ^
     *  |
     *  5
     */

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Ready(Unit)
        )
    ),

    // ================================================================================================================

    // Error test cases ===============================================================================================
    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    /*  ^
     *  |
     *  10
     */

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    /*  ^
     *  |
     *  15
     */

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),
    // ================================================================================================================

    // Error followed by success test cases ===========================================================================
    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    /*  ^
     *  |
     *  20
     */

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        expectedOutput = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),
    // ================================================================================================================

    // Success followed by error test cases ===========================================================================
    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    /*  ^
     *  |
     *  25
     */

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetCached,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = false,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    /*  ^
     *  |
     *  30
     */

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = false,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Error(Unit, exampleException)
        )
    ),

    CachingLoadingErrorWithMultipleSubscribersTestCase(
        caching = true,
        autoLoading = true,
        command = RepositoryCommand.GetFresh,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        expectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        expectedOutput2 = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Error(Unit, exampleException)
        )
    )
)
