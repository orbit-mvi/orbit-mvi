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

data class RepositoryMulticastingToPreviousSubscribersTestCase(
    val id: Int = testCaseId,
    val caching: Boolean,
    val executorOutput: List<ResourceStream<Unit>> =
        listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit))
        ),
    val firstCommand: RepositoryCommand,
    val secondCommand: RepositoryCommand,
    val firstExpectedOutputInitial: List<ResourceStatus<Unit>>,
    val secondExpectedOutput: List<ResourceStatus<Unit>>,
    val firstExpectedOutputAfterSecondSubscription: List<ResourceStatus<Unit>>
)

// Exceptions are always unique, assertions fail if we don't use the same one
private val exampleException: Throwable = IllegalStateException()
private var testCaseId: Int = 1
    get() = field++

fun buildRepositoryMultipleSubscribersInSequenceTestCases() = listOf(
    //  Happy path test cases =========================================================================================

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = emptyList(),
        secondExpectedOutput = listOf(ResourceStatus.Ready(Unit))
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = emptyList(),
        secondExpectedOutput = listOf(ResourceStatus.Ready(Unit))
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    /*  ^
     *  |
     *  5
     */

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    // ================================================================================================================

    // Error test cases ===============================================================================================

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    /*  ^
     *  |
     *  10
     */

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    /*  ^
     *  |
     *  15
     */

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    // ================================================================================================================

    // Error followed by success test cases ===========================================================================

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))

        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    /*  ^
     *  |
     *  20
     */

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException)),
            Observable.just(ResourceStatus.Ready(Unit))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    // ================================================================================================================

    // Success followed by error test cases ===========================================================================

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))

        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = emptyList(),
        secondExpectedOutput = listOf(ResourceStatus.Ready(Unit))
    ),

    /*  ^
     *  |
     *  25
     */

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Error(Unit, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Error(Unit, exampleException)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = emptyList(),
        secondExpectedOutput = listOf(ResourceStatus.Ready(Unit))
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = true,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Error(Unit, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Error(Unit, exampleException)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))

        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    /*  ^
     *  |
     *  30
     */

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    ),

    RepositoryMulticastingToPreviousSubscribersTestCase(
        caching = false,
        executorOutput = listOf<ResourceStream<Unit>>(
            Observable.just(ResourceStatus.Ready(Unit)),
            Observable.just(ResourceStatus.Error<Unit>(null, exampleException))
        ),
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Error<Unit>(null, exampleException)
        )
    )
)
