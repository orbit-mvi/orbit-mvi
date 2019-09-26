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

import assertk.assertThat
import assertk.assertions.containsExactly
import com.babylon.orbit.domain.collections.LRUCache
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.lifecycle.CachingMode

internal class RepositorySpek : Spek({

    val mockExecutorFactory by memoized { mock<(Unit) -> ResourceStream<Unit>>() }

    given("single subscriber unique key test cases") {
        val uniqueKeyResourceTestCases = buildSingleSubscriberUniqueKeyResourceTestCases()

        beforeGroup { RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() } }

        uniqueKeyResourceTestCases.forEach { testCase ->
            given("${testCase.id}: caching is ${testCase.caching}") {

                given("automatic loading status emission is ${testCase.autoLoading}") {
                    val autoLoading by memoized { testCase.autoLoading }

                    given("an executor emitting ${testCase.executorOutput.blockingIterable().joinToString(" ", "[", "]")}") {
                        val repository: Repository<Unit, Unit> by memoized {
                            object : Repository<Unit, Unit>(
                                cacheEnabled = testCase.caching,
                                autoLoadingStatus = autoLoading,
                                wrapUncaughtThrowables = true,
                                getExecutorFactory = mockExecutorFactory,
                                dataStreamCache = LRUCache(20)
                            ) {}
                        }

                        beforeEachTest {
                            whenever(mockExecutorFactory.invoke(Unit)).thenReturn(testCase.executorOutput)
                        }
                        on("subscribing via sending ${testCase.command} command") {
                            val testObserver = repository.executeAndSubscribe(Unit, testCase.command).test()

                            it("emits ${testCase.expectedOutput.joinToString(" ", "[", "]")}") {
                                println("Expected: ${testCase.expectedOutput.joinToString(" ", "[", "]")}")
                                println("Actual  : ${testObserver?.values()?.joinToString(" ", "[", "]")}")
                                assertThat(testObserver.values()).containsExactly(*testCase.expectedOutput.toTypedArray())
                            }
                        }
                    }
                }
            }
        }

        afterGroup { RxJavaPlugins.reset() }
    }

    given("multiple subscribers, test cases around caching, loading and errors") {
        val uniqueKeyResourceTestCases = buildCachingLoadingErrorWithMultipleSubscribersTestCases()

        beforeGroup {
            RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        }

        uniqueKeyResourceTestCases.forEach { testCase ->
            given("${testCase.id}: caching is ${testCase.caching}") {
                given("automatic loading status emission is ${testCase.autoLoading}") {
                    val autoLoading by memoized { testCase.autoLoading }

                    given("an executor emitting ${testCase.executorOutput.toDescriptionString()}") {
                        val repository: Repository<Unit, Unit> by memoized(mode = CachingMode.SCOPE) {
                            object : Repository<Unit, Unit>(
                                cacheEnabled = testCase.caching,
                                autoLoadingStatus = autoLoading,
                                wrapUncaughtThrowables = true,
                                getExecutorFactory = mockExecutorFactory,
                                dataStreamCache = LRUCache(20)
                            ) {}
                        }

                        beforeGroup {
                            whenever(mockExecutorFactory.invoke(Unit))
                                .thenReturn(
                                    testCase.executorOutput[0],
                                    *testCase.executorOutput.subList(1, testCase.executorOutput.size).toTypedArray()
                                )
                        }
                        given("subscribing via sending ${testCase.command} command") {
                            var testObserver: TestObserver<ResourceStatus<Unit>>? = null
                            beforeGroup {
                                testObserver = repository.executeAndSubscribe(Unit, testCase.command).test()
                            }

                            it("emits ${testCase.expectedOutput.joinToString(" ", "[", "]")}") {
                                println("Expected: ${testCase.expectedOutput.joinToString(" ", "[", "]")}")
                                println("Actual  : ${testObserver?.values()?.joinToString(" ", "[", "]")}")
                                testObserver?.values()?.run { assertThat(this).containsExactly(*testCase.expectedOutput.toTypedArray()) }
                            }

                            given("observer is unsubscribed and resubscribed") {
                                var testObserver2: TestObserver<ResourceStatus<Unit>>? = null
                                beforeGroup {
                                    //                                    testObserver?.dispose()
                                    testObserver2 = repository.executeAndSubscribe(Unit, testCase.command).test()
                                    testObserver2?.assertSubscribed()
                                }

                                it("emits ${testCase.expectedOutput2.joinToString(" ", "[", "]")}") {
                                    println("Expected: ${testCase.expectedOutput2.joinToString(" ", "[", "]")}")
                                    println("Actual  : ${testObserver2?.values()?.joinToString(" ", "[", "]")}")
                                    testObserver2?.values()?.run { assertThat(this).containsExactly(*testCase.expectedOutput2.toTypedArray()) }
                                }
                            }
                        }
                    }
                }
            }
        }
        afterGroup { RxJavaPlugins.reset() }
    }

    given("tests checking how multiple subscribers affect each other") {
        val uniqueKeyResourceTestCases = buildRepositoryMultipleSubscribersInSequenceTestCases()

        beforeGroup { RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() } }

        uniqueKeyResourceTestCases.forEach { testCase ->
            given("${testCase.id}: an executor emitting ${testCase.executorOutput.toDescriptionString()}") {
                val repository: Repository<Unit, Unit> by memoized(mode = CachingMode.SCOPE) {
                    object : Repository<Unit, Unit>(
                        cacheEnabled = testCase.caching,
                        autoLoadingStatus = true,
                        wrapUncaughtThrowables = true,
                        getExecutorFactory = mockExecutorFactory,
                        dataStreamCache = LRUCache(20)
                    ) {}
                }

                beforeGroup {
                    whenever(mockExecutorFactory.invoke(Unit))
                        .thenReturn(testCase.executorOutput[0], *testCase.executorOutput.subList(1, testCase.executorOutput.size).toTypedArray())
                }
                given("caching is ${testCase.caching}") {
                    given("first observer subscribes via ${testCase.firstCommand}") {
                        var testObserver: TestObserver<ResourceStatus<Unit>>? = null
                        var firstObserverInitialValueCount = 0
                        beforeGroup {
                            testObserver = repository.executeAndSubscribe(Unit, testCase.firstCommand).test()
                            firstObserverInitialValueCount = testObserver!!.valueCount()
                        }

                        it("emits to the first subscriber: ${testCase.firstExpectedOutputInitial.joinToString(" ", "[", "]")}") {
                            println("Expected: ${testCase.firstExpectedOutputInitial.joinToString(" ", "[", "]")}")
                            println("Actual  : ${testObserver?.values()?.joinToString(" ", "[", "]")}")
                            testObserver?.values()?.run { assertThat(this).containsExactly(*testCase.firstExpectedOutputInitial.toTypedArray()) }
                        }

                        given("second observer is subscribed via ${testCase.secondCommand}") {
                            var testObserver2: TestObserver<ResourceStatus<Unit>>? = null
                            beforeGroup {
                                testObserver2 = repository.executeAndSubscribe(Unit, testCase.secondCommand).test()
                            }

                            it(
                                "emits to the first subscriber:  ${testCase.firstExpectedOutputAfterSecondSubscription
                                    .joinToString(" ", "[", "]")}"
                            ) {
                                val laterEmissions = testObserver?.values()?.subList(firstObserverInitialValueCount, testObserver!!.valueCount())
                                println("Expected: ${testCase.firstExpectedOutputAfterSecondSubscription.joinToString(" ", "[", "]")}")
                                println("Actual  : ${laterEmissions?.joinToString(" ", "[", "]")}")
                                laterEmissions?.run {
                                    assertThat(this).containsExactly(*testCase.firstExpectedOutputAfterSecondSubscription.toTypedArray())
                                }
                            }
                            it("emits to the second subscriber:  ${testCase.secondExpectedOutput.joinToString(" ", "[", "]")}") {
                                println("Expected: ${testCase.secondExpectedOutput.joinToString(" ", "[", "]")}")
                                println("Actual  : ${testObserver2?.values()?.joinToString(" ", "[", "]")}")
                                testObserver2?.values()?.run { assertThat(this).containsExactly(*testCase.secondExpectedOutput.toTypedArray()) }
                            }
                        }
                    }
                }
            }
        }
        afterGroup { RxJavaPlugins.reset() }
    }
})
