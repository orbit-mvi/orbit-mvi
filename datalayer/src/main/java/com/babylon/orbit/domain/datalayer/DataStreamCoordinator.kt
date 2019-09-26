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

import com.jakewharton.rxrelay2.PublishRelay
import com.nytimes.android.external.cache3.Cache
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

class DataStreamCoordinator<REQUEST, RESULT>(
    private val request: REQUEST,
    private val getExecutorFactory: (REQUEST) -> ResourceStream<RESULT>,
    private val updateExecutorFactory: ((RESULT) -> ResourceStream<RESULT>)?,
    private val cache: Cache<REQUEST, RESULT>,
    private val autoLoadingStatus: Boolean,
    private val wrapUncaughtThrowables: Boolean
) {
    private val requestReadWriteLock = ReentrantLock()
    @Volatile
    private var inflightGetRequestStream: ResourceStream<RESULT>? = null
    @Volatile
    private var inflightGetRequestDisposable: Disposable? = null
    @Volatile
    private var inflightUpdateRequestStream: ResourceStream<RESULT>? = null
    private var cacheInvalidated: AtomicBoolean = AtomicBoolean(false)
    private var responseStreamDisposable: Disposable? = null
    val commandRelay = PublishRelay.create<RepositoryCommand>()
    val responseStream: Observable<ResourceStatus<RESULT>>

    init {
        responseStream = commandRelay
            .publish {
                Observable.merge(
                    it.flatMap { combinedGetDataSources(it) },
                    applyUpdateStream(it) // This is outside of the combined data sources to be able to queue update requests
                )
            }
            .publish()
            .autoConnect(1) {
                // Connect as soon as it's created
                responseStreamDisposable = it
            }
    }

    fun dispose() {
        responseStreamDisposable?.dispose()
    }

    /**
     * This method routes the original requests to all of the streams responsible for command types
     */
    private fun combinedGetDataSources(repositoryCommand: RepositoryCommand): ResourceStream<RESULT> {
        return Observable.just(repositoryCommand)
            .publish { requestObs ->
                Observable.merge(
                    listOf(
                        requestObs.compose(::applyInvalidateWithCacheBlockStream),
                        requestObs.compose(::applyInvalidateStream),
                        requestObs.compose(::applyGetCachedStream),
                        requestObs.compose(::applyGetFreshStream)
                    )
                )
            }
    }

    /**
     * This stream will react to the [RepositoryCommand.Update] cache-control only. Requests travelling
     * down this stream will cancel any in progress get requests and run a update request. Calling this multiple times
     * will queue the incoming update requests.
     *
     * Any new or old subscribers will connect to the update requests currently in progress. It is assumed that the result
     * of the update request will be up-to-date.
     */
    private fun applyUpdateStream(commandObs: Observable<RepositoryCommand>): ResourceStream<RESULT> =
        commandObs.observeOn(Schedulers.io())
            .ofType<RepositoryCommand.Update<RESULT>>()
            .doOnNext {
                if (updateExecutorFactory != null) {
                    stopGetRequest()
                    cacheInvalidated.compareAndSet(true, false)
                }
            }
            .flatMap { resolveUpdateNetworkRequestObservable(it.data) }

    /**
     * This stream will react to the [RepositoryCommand.InvalidateWithCacheBlock] command only. Requests travelling
     * down this stream will clear the cache, cancel any in progress get requests and run a new get request or connect
     * to an existing update request.
     *
     * Additionally it blocks writes to the cache until a new command other than this one is sent to the repository. This
     * is to temporarily prevent any writes to the cache in cases like logging out etc.
     */
    private fun applyInvalidateWithCacheBlockStream(commandObs: Observable<RepositoryCommand>): ResourceStream<RESULT> {
        return commandObs.observeOn(Schedulers.io())
            .ofType<RepositoryCommand.InvalidateWithCacheBlock>()
            .doOnNext {
                clearCacheAndStopGetRequest()
                cacheInvalidated.compareAndSet(false, true) // Block cache updates until someone requests again
            }
            .switchMap {
                getNetworkStream()
            }
    }

    /**
     * This stream will react to the [RepositoryCommand.Invalidate] command only. Requests travelling
     * down this stream will clear the cache, cancel any in progress get requests and run a new get request or connect
     * to an existing update request.
     */
    private fun applyInvalidateStream(commandObs: Observable<RepositoryCommand>): ResourceStream<RESULT> {
        return commandObs.observeOn(Schedulers.io())
            .ofType<RepositoryCommand.Invalidate>()
            .doOnNext {
                clearCacheAndStopGetRequest()
                cacheInvalidated.compareAndSet(true, false)
            }
            .switchMap {
                getNetworkStream()
            }
    }

    /**
     * This stream will react to the [RepositoryCommand.GetCached] command only.
     * Requests travelling down this stream will connect to an in progress update or get request.
     * If no request is in progress, it will emit from the cache if possible, otherwise launch a new get request.
     */
    private fun applyGetCachedStream(commandObs: Observable<RepositoryCommand>): ResourceStream<RESULT> {
        return commandObs.observeOn(Schedulers.io())
            .ofType<RepositoryCommand.GetCached>()
            .doOnNext {
                cacheInvalidated.compareAndSet(true, false)
            }
            .switchMap {
                getInProgressNetworkStream()
                    .switchIfEmpty(
                        combinedCacheStream()
                            .map { ResourceStatus.Ready(it) as ResourceStatus<RESULT> }
                            .toObservable()
                    )
                    .switchIfEmpty(getNetworkStream())
            }
    }

    /**
     * This stream will react to the [RepositoryCommand.GetFresh] command only.
     * Requests travelling down this stream will connect to an in progress update or get request. If no request is in progress,
     * it will launch a new get request.
     */
    private fun applyGetFreshStream(commandObs: Observable<RepositoryCommand>): ResourceStream<RESULT> {
        return commandObs.observeOn(Schedulers.io())
            .ofType<RepositoryCommand.GetFresh>()
            .doOnNext {
                cacheInvalidated.compareAndSet(true, false)
            }
            .switchMap { getNetworkStream() }
    }

    private fun clearCacheAndStopGetRequest() {
        cache.invalidate(request)
        stopGetRequest()
    }

    private fun stopGetRequest() {
        requestReadWriteLock.lock()
        inflightGetRequestStream = null
        inflightGetRequestDisposable?.dispose()
        inflightGetRequestDisposable = null
        requestReadWriteLock.unlock()
    }

    // This function will eventually combine a cache and database
    private fun combinedCacheStream() = cacheStream

    private fun getInProgressNetworkStream(): ResourceStream<RESULT> =
        inflightUpdateRequestStream ?: inflightGetRequestStream ?: Observable.empty()

    private fun getNetworkStream(): ResourceStream<RESULT> =
        Observable.just(Unit)
            .switchMap {
                resolveGetNetworkRequestObservable()
            }

    /**
     * Returns a network request observable, creating one if necessary.
     * If one is already present and not completed, it's returned, otherwise a new one is created.
     *
     * Update requests take priority over get requests as they are the ones that will return the most up-to-date results.
     *
     * This function is blocking to avoid creating multiple competing network requests.
     *
     * @return A network request observable
     */
    private fun resolveGetNetworkRequestObservable(): ResourceStream<RESULT> {
        requestReadWriteLock.lock()
        val requestObs = inflightUpdateRequestStream ?: inflightGetRequestStream
        ?: createNewGetNetworkRequestObservable().apply {
            inflightGetRequestStream = this
        }
        requestReadWriteLock.unlock()
        return requestObs
    }

    /**
     * Returns a network request observable, creating one if necessary.
     * If one is already present and not completed, it's returned, otherwise a new one is created.
     *
     * This function is blocking to avoid creating multiple competing network requests.
     *
     * @return A network request observable
     */
    private fun resolveUpdateNetworkRequestObservable(result: RESULT): ResourceStream<RESULT> {
        requestReadWriteLock.lock()
        val requestObs = inflightUpdateRequestStream
            ?: createNewUpdateNetworkRequestObservable(result)?.apply {
                inflightUpdateRequestStream = this
            }
            ?: Observable.empty()
        requestReadWriteLock.unlock()
        return requestObs
    }

    /**
     * Creates a new shared network request observable.
     *
     * In the stream, the latest cache value is appended to the [ResourceStatus] coming from the repository only if:
     * 1. The [ResourceStatus.currentData] field is null
     * 2. There is a cached value present for our request
     *
     * This allows us to always emit the latest cached value when resources are [ResourceStatus.Loading].
     * Subscribers who join _while_ a network request is being executed will get the current cached value
     * immediately along with [ResourceStatus.Loading].
     *
     * @return A network request observable
     */
    private fun createNewGetNetworkRequestObservable(): ResourceStream<RESULT> {
        // Create a cold observable for the network request
        return getExecutorFactory(request)
            .doOnSubscribe { inflightGetRequestDisposable = it }
            .doOnNext { status -> updateCache(status.currentData) }
            .flatMapMaybe { status ->
                combinedCacheStream()
                    .map {
                        when (status) {
                            is ResourceStatus.Ready<RESULT> -> ResourceStatus.Ready(it)
                            is ResourceStatus.Loading<RESULT> -> ResourceStatus.Loading(it)
                            is ResourceStatus.Error<RESULT> -> ResourceStatus.Error(it, status.error)
                        }
                    }
                    .defaultIfEmpty(status)
            }
            .doAfterTerminate {
                requestReadWriteLock.lock()
                inflightGetRequestStream = null
                requestReadWriteLock.unlock()
            }
            .onErrorResumeNext { throwable: Throwable -> getErrorHandler(throwable) }
            .replay(1)
            .autoConnect()
            .let {
                getLoadingStatus().concatWith(it)
            }
    }

    /**
     * Creates a new shared network request observable.
     *
     * In the stream, the latest cache value is appended to the [ResourceStatus] coming from the repository only if:
     * 1. The [ResourceStatus.currentData] field is null
     * 2. There is a cached value present for our request
     *
     * This allows us to always emit the latest cached value when resources are [ResourceStatus.Loading].
     * Subscribers who join _while_ a network request is being executed will get the current cached value
     * immediately along with [ResourceStatus.Loading].
     *
     * @return A network request observable
     */
    private fun createNewUpdateNetworkRequestObservable(setTo: RESULT): ResourceStream<RESULT>? {
        // Create a cold observable for the network request
        return updateExecutorFactory?.invoke(setTo)
            ?.doOnNext { status -> updateCache(status.currentData) }
            ?.flatMapMaybe { status ->
                combinedCacheStream()
                    .map {
                        when (status) {
                            is ResourceStatus.Ready<RESULT> -> ResourceStatus.Ready(it)
                            is ResourceStatus.Loading<RESULT> -> ResourceStatus.Loading(it)
                            is ResourceStatus.Error<RESULT> -> ResourceStatus.Error(it, status.error)
                        }
                    }
                    .defaultIfEmpty(status)
            }
            ?.doAfterTerminate {
                requestReadWriteLock.lock()
                inflightUpdateRequestStream = null
                requestReadWriteLock.unlock()
            }
            ?.onErrorResumeNext { throwable: Throwable -> getErrorHandler(throwable) }
            ?.replay(1)
            ?.autoConnect()
            ?.let {
                getLoadingStatus().concatWith(it)
            }
    }

    /**
     * If wrapping is turned on (it is by default), the throwable will be automatically wrapped to avoid sending an
     * error down the flow chain and breaking it. Using [wrapUncaughtThrowables] is recommended unless you really
     * want your chain to break.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getErrorHandler(throwable: Throwable): ResourceStream<RESULT> {
        return if (wrapUncaughtThrowables) {
            Observable.just(ResourceStatus.Error<RESULT>(null, throwable))
        } else {
            Observable.error<ResourceStatus<RESULT>>(throwable)
        }
    }

    /**
     * Returns the loading status to emit before starting a network request.
     */
    @Suppress("UNCHECKED_CAST")
    private fun getLoadingStatus(): ResourceStream<RESULT> =
        when (autoLoadingStatus) {
            true -> combinedCacheStream()
                .map { ResourceStatus.Loading(it) }
                .defaultIfEmpty(ResourceStatus.Loading(null))
                .toObservable()
                .cast()
            false -> Observable.empty<ResourceStatus<RESULT>>()
        }

    private val cacheStream: Maybe<RESULT> = Maybe.fromCallable<RESULT> {
        cache.getIfPresent(request)
    }

    private fun updateCache(result: RESULT?) {
        if (cacheInvalidated.get().not() && result != null) {
            cache.put(request, result)
        }
    }
}
