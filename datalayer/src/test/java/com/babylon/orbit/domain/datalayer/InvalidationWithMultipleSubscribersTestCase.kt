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

data class InvalidationWithMultipleSubscribersTestCase(
    val id: Int = testCaseId,
    val caching: Boolean,
    val cachedValue: Unit? = null,
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

private var testCaseId: Int = 1
    get() = field++

fun buildInvalidationWithMultipleSubscribersTestCases() = listOf(
    //  No refresh invalidation: Empty cache test cases ================================================================

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.InvalidateWithCacheBlock,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.InvalidateWithCacheBlock,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.InvalidateWithCacheBlock,
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

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.InvalidateWithCacheBlock,
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

    InvalidationWithMultipleSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.InvalidateWithCacheBlock,
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

    InvalidationWithMultipleSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.InvalidateWithCacheBlock,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.InvalidateWithCacheBlock,
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

    InvalidationWithMultipleSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.InvalidateWithCacheBlock,
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

    //  No refresh invalidation: Non-Empty cache test cases ============================================================

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        cachedValue = Unit,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.InvalidateWithCacheBlock,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        cachedValue = Unit,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.InvalidateWithCacheBlock,
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
     *  10
     */

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        cachedValue = Unit,
        firstCommand = RepositoryCommand.InvalidateWithCacheBlock,
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

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        cachedValue = Unit,
        firstCommand = RepositoryCommand.InvalidateWithCacheBlock,
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

    //  Refresh invalidation: Empty cache test cases ================================================================

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.Invalidate,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.Invalidate,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.Invalidate,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    /*  ^
     *  |
     *  15
     */

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        firstCommand = RepositoryCommand.Invalidate,
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

    InvalidationWithMultipleSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.Invalidate,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.Invalidate,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.Invalidate,
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

    InvalidationWithMultipleSubscribersTestCase(
        caching = false,
        firstCommand = RepositoryCommand.Invalidate,
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

    /*  ^
     *  |
     *  20
     */

    //  No refresh invalidation: Non-Empty cache test cases ============================================================

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        cachedValue = Unit,
        firstCommand = RepositoryCommand.GetCached,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.Invalidate,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        cachedValue = Unit,
        firstCommand = RepositoryCommand.GetFresh,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading(Unit),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.Invalidate,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        cachedValue = Unit,
        firstCommand = RepositoryCommand.Invalidate,
        firstExpectedOutputInitial = listOf(
            ResourceStatus.Loading<Unit>(null),
            ResourceStatus.Ready(Unit)
        ),
        secondCommand = RepositoryCommand.GetCached,
        firstExpectedOutputAfterSecondSubscription = listOf(
            ResourceStatus.Ready(Unit)
        ),
        secondExpectedOutput = listOf(
            ResourceStatus.Ready(Unit)
        )
    ),

    InvalidationWithMultipleSubscribersTestCase(
        caching = true,
        cachedValue = Unit,
        firstCommand = RepositoryCommand.Invalidate,
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
    )
)
