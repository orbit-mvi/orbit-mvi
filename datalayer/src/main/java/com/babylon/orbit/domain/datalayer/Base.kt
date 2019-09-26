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

sealed class RepositoryCommand {
    /**
    Will always emit from the cache if present, otherwise will run a network request
     */
    object GetCached : RepositoryCommand()

    /**
    Will emit from the cache if present and will run a network request afterwards
     */
    object GetFresh : RepositoryCommand()

    /**
    Will stop any running network request and clear the cached value. Then it will run a request to refresh that data.
    Additionally it blocks writes to the cache until a new command other than this one is sent to the repository. This
    is to temporarily prevent any writes to the cache in cases like logging out etc.
     */
    object InvalidateWithCacheBlock : RepositoryCommand()

    /**
    Will stop any running network request and clear the cached value. Then it will run a request to refresh that data.
     */
    object Invalidate : RepositoryCommand()

    /**
    Will run a network request to update the value on the backend and then update the cache.
     */
    data class Update<T>(val data: T) : RepositoryCommand()
}

sealed class ResourceStatus<DATA> {
    open val currentData: DATA? = null

    data class Ready<DATA>(override val currentData: DATA) : ResourceStatus<DATA>()
    data class Loading<DATA>(override val currentData: DATA?) : ResourceStatus<DATA>()
    data class Error<DATA>(override val currentData: DATA?, val error: Throwable) : ResourceStatus<DATA>()
}

typealias ResourceStream<RESULT> = Observable<ResourceStatus<RESULT>>
