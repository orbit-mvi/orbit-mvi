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

import com.babylon.orbit.domain.collections.DummyCache
import com.babylon.orbit.domain.collections.LRUCache
import com.nytimes.android.external.cache3.Cache
import com.nytimes.android.external.cache3.CacheBuilder
import hu.akarnokd.rxjava2.subjects.DispatchWorkSubject
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@Deprecated("Orbit repositories are overcomplicated and not to be used any more")
abstract class Repository<REQUEST, RESULT>(
    private val cacheEnabled: Boolean = true,
    private val autoLoadingStatus: Boolean = true,
    private val wrapUncaughtThrowables: Boolean = true,
    private val storage: Storage<RESULT>? = null, // TODO start supporting DB
    private var getExecutorFactory: ((REQUEST) -> ResourceStream<RESULT>)? = null,
    private var updateExecutorFactory: ((RESULT) -> ResourceStream<RESULT>)? = null,
    private val dataStreamCache: LRUCache<Any, DataStreamCoordinator<*, *>> = LRUCache(3)
) {
    private val cache: Cache<REQUEST, RESULT> by lazy { createSimpleCache() }

    fun clearCacheNoRefresh() {
        synchronized(dataStreamCache) {
            dataStreamCache.values.forEach {
                it.apply {
                    responseStream.subscribe().dispose() // Make sure the repository is connected
                    commandRelay.accept(RepositoryCommand.InvalidateWithCacheBlock)
                }
            }
        }
    }

    /**
     * Returns a LIVE observable already subscribed to the data stream that caches all emissions from this point until
     * it is subscribed to. In order to avoid memory leaks it is therefore extremely important to dispose of this properly
     * when no longer needed.
     */
    fun executeAndSubscribe(request: REQUEST, repositoryCommand: RepositoryCommand = RepositoryCommand.GetCached): ResourceStream<RESULT> {
        val (dataStream, output) = obtainIndividualDataStream(request)
        dataStream.commandRelay.accept(repositoryCommand)
        return output
    }

    /**
     * Fires off the request to the data stream, but doesn't listen to the results. Useful to affect other subscribers
     * without having to subscribe to the stream.
     */
    fun execute(request: REQUEST, repositoryCommand: RepositoryCommand) {
        val (dataStream, output) = obtainIndividualDataStream(request)
        output.subscribe().dispose() // Make sure the repository is connected
        dataStream.commandRelay.accept(repositoryCommand)
    }

    private fun obtainIndividualDataStream(request: REQUEST): Pair<DataStreamCoordinator<REQUEST, RESULT>, ResourceStream<RESULT>> {
        val combinedStream = getCoordinator(request)
        val outputSubject: DispatchWorkSubject<ResourceStatus<RESULT>> = DispatchWorkSubject.create(Schedulers.io())

        /* Subscribe to updates using a caching subject. Otherwise consumer could miss some updates due to a race condition.
         * When the consumer emits a request we would like to subscribe the consumer to updates and then send that
         * request to be processed. Unless we use a caching subject this is impossible without dropping some responses
         * in the process.
        */
        val disposable = combinedStream.responseStream
            .doAfterTerminate {
                dataStreamCache.remove(request as Any)
            }
            .distinctUntilChanged()
            .subscribe(outputSubject::onNext, outputSubject::onError, outputSubject::onComplete, outputSubject::onSubscribe)

        return combinedStream to outputSubject
            .doOnDispose { disposable.dispose() }
    }

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    private fun getCoordinator(request: REQUEST): DataStreamCoordinator<REQUEST, RESULT> {
        val getExecutorFactory = getExecutorFactory
            ?: throw IllegalStateException() // TODO this is not a good way to do this

        synchronized(dataStreamCache) {
            return dataStreamCache.getOrPut(this to request as Any,
                { DataStreamCoordinator(request, getExecutorFactory, updateExecutorFactory, cache, autoLoadingStatus, wrapUncaughtThrowables) })
                    as DataStreamCoordinator<REQUEST, RESULT>
        }
    }

    private fun createSimpleCache() =
        if (cacheEnabled) {
            CacheBuilder.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(100L)
                .build<REQUEST, RESULT>()
        } else DummyCache()
}
