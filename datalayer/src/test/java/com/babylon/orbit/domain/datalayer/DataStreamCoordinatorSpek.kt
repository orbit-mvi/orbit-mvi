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
import com.babylon.orbit.domain.collections.DummyCache
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.nytimes.android.external.cache3.CacheBuilder
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.lifecycle.CachingMode

class DataStreamCoordinatorSpek : Spek({

    val mockExecutorFactory by memoized { mock<(Unit) -> ResourceStream<Unit>>() }

    given("single subscriber unique key test cases") {
        val testCases = buildSingleSubscriberUniqueKeyResourceTestCases()

        beforeGroup { RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() } }

        testCases.forEach { testCase ->
            given("${testCase.id}: caching is ${testCase.caching}") {
                val cache by memoized {
                    if (testCase.caching) {
                        CacheBuilder.newBuilder().build<Unit, Unit>()
                    } else {
                        DummyCache<Unit, Unit>()
                    }
                }
                given("automatic loading status emission is ${testCase.autoLoading}") {
                    val autoLoading by memoized { testCase.autoLoading }

                    given("an executor emitting ${testCase.executorOutput.blockingIterable().joinToString(" ", "[", "]")}") {
                        val coordinator by memoized {
                            DataStreamCoordinator(Unit, mockExecutorFactory, null, cache, autoLoading, wrapUncaughtThrowables = true)
                        }

                        beforeEachTest {
                            whenever(mockExecutorFactory.invoke(Unit)).thenReturn(testCase.executorOutput)
                        }
                        on("subscribing via sending ${testCase.command} command") {
                            val testObserver = coordinator.responseStream.test()
                            coordinator.commandRelay.accept(testCase.command)

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
        val testCases = buildCachingLoadingErrorWithMultipleSubscribersTestCases()

        beforeGroup { RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() } }

        testCases.filter { it.id == 4 }.forEach { testCase ->
            given("${testCase.id}: caching is ${testCase.caching}") {
                val cache by memoized {
                    if (testCase.caching) {
                        CacheBuilder.newBuilder().build<Unit, Unit>()
                    } else {
                        DummyCache<Unit, Unit>()
                    }
                }
                given("automatic loading status emission is ${testCase.autoLoading}") {
                    val autoLoading by memoized { testCase.autoLoading }

                    given("an executor emitting ${testCase.executorOutput.toDescriptionString()}") {
                        val coordinator by memoized(mode = CachingMode.SCOPE) {
                            DataStreamCoordinator(
                                Unit,
                                mockExecutorFactory,
                                null,
                                cache,
                                autoLoading,
                                wrapUncaughtThrowables = true
                            )
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
                                testObserver = coordinator.responseStream.test()
                                coordinator.commandRelay.accept(testCase.command)
                            }

                            it("emits ${testCase.expectedOutput.joinToString(" ", "[", "]")}") {
                                println("Expected: ${testCase.expectedOutput.joinToString(" ", "[", "]")}")
                                println("Actual  : ${testObserver?.values()?.joinToString(" ", "[", "]")}")
                                testObserver?.values()?.run { assertThat(this).containsExactly(*testCase.expectedOutput.toTypedArray()) }
                            }

                            given("observer is unsubscribed and resubscribed") {
                                var testObserver2: TestObserver<ResourceStatus<Unit>>? = null
                                beforeGroup {
                                    testObserver2 = coordinator.responseStream.test()
                                    coordinator.commandRelay.accept(testCase.command)
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
        val testCases = buildMultipleSubscribersInSequenceTestCases()

        beforeGroup { RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() } }

        testCases.forEach { testCase ->
            given("${testCase.id}: caching is ${testCase.caching}") {
                val cache by memoized {
                    if (testCase.caching) {
                        CacheBuilder.newBuilder().build<Unit, Unit>()
                    } else {
                        DummyCache<Unit, Unit>()
                    }
                }
                given("an executor emitting ${testCase.executorOutput.toDescriptionString()}") {
                    val coordinator by memoized(mode = CachingMode.SCOPE) {
                        DataStreamCoordinator(
                            Unit,
                            mockExecutorFactory,
                            null,
                            cache,
                            autoLoadingStatus = true,
                            wrapUncaughtThrowables = true
                        )
                    }

                    beforeGroup {
                        whenever(mockExecutorFactory.invoke(Unit))
                            .thenReturn(
                                testCase.executorOutput[0],
                                *testCase.executorOutput.subList(1, testCase.executorOutput.size).toTypedArray()
                            )
                    }
                    given("first observer is subscribed via ${testCase.firstCommand}") {
                        var testObserver: TestObserver<ResourceStatus<Unit>>? = null
                        var firstObserverInitialValueCount = 0
                        beforeGroup {
                            testObserver = coordinator.responseStream.test()
                            coordinator.commandRelay.accept(testCase.firstCommand)
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
                                testObserver2 = coordinator.responseStream.test()
                                coordinator.commandRelay.accept(testCase.secondCommand)
                            }

                            it("emits to the second subscriber:  ${testCase.secondExpectedOutput.joinToString(" ", "[", "]")}") {
                                println("Expected: ${testCase.secondExpectedOutput.joinToString(" ", "[", "]")}")
                                println("Actual  : ${testObserver2?.values()?.joinToString(" ", "[", "]")}")
                                testObserver2?.values()?.run { assertThat(this).containsExactly(*testCase.secondExpectedOutput.toTypedArray()) }
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
                        }
                    }
                }
            }
        }
        afterGroup { RxJavaPlugins.reset() }
    }

    given("loading in progress test cases") {
        val testCases = buildLoadingInProgressTestCases()

        testCases.forEach { testCase ->
            given("${testCase.id}: automatic loading status emission is ${testCase.autoLoading}") {
                val autoLoading by memoized { testCase.autoLoading }

                given("an executor that is loading forever") {
                    val coordinator by memoized(mode = CachingMode.SCOPE) {
                        DataStreamCoordinator(
                            Unit,
                            mockExecutorFactory,
                            null,
                            CacheBuilder.newBuilder()
                                .build(),
                            autoLoading,
                            wrapUncaughtThrowables = true
                        )
                    }

                    beforeGroup {
                        whenever(mockExecutorFactory.invoke(Unit)).thenReturn(Observable.never())
                    }
                    given("subscribing via sending ${testCase.command} command") {
                        var testObserver: TestObserver<ResourceStatus<Unit>>? = null
                        beforeGroup {
                            testObserver = coordinator.responseStream.test()
                            coordinator.commandRelay.accept(testCase.command)
                        }

                        it("emits ${testCase.expectedOutput.joinToString(" ", "[", "]")}") {
                            Thread.sleep(50) // Stupid lazy sleep. Should be using a test scheduler but don't want to waste more time on this
                            println("Expected: ${testCase.expectedOutput.joinToString(" ", "[", "]")}")
                            println("Actual  : ${testObserver?.values()?.joinToString(" ", "[", "]")}")
                            testObserver?.values()?.run { assertThat(this).containsExactly(*testCase.expectedOutput.toTypedArray()) }
                        }

                        given("observer is unsubscribed and resubscribed") {
                            var testObserver2: TestObserver<ResourceStatus<Unit>>? = null
                            beforeGroup {
                                testObserver2 = coordinator.responseStream.test()
                                coordinator.commandRelay.accept(testCase.command)
                            }

                            it("emits ${testCase.expectedOutput.joinToString(" ", "[", "]")}") {
                                Thread.sleep(50) // Stupid lazy sleep. Should be using a test scheduler but don't want to waste more time on this
                                println("Expected: ${testCase.expectedOutput.joinToString(" ", "[", "]")}")
                                println("Actual  : ${testObserver2?.values()?.joinToString(" ", "[", "]")}")
                                testObserver2?.values()?.run { assertThat(this).containsExactly(*testCase.expectedOutput.toTypedArray()) }
                            }
                        }
                    }
                }
            }
        }
    }

    given("test cases for invalidation and multiple subscribers") {
        val testCases = buildInvalidationWithMultipleSubscribersTestCases()

        beforeGroup { RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() } }

        testCases.forEach { testCase ->

            given("${testCase.id}: caching is ${testCase.caching}") {
                val cacheSpy by memoized(mode = CachingMode.SCOPE) {
                    spy(
                        if (testCase.caching) {
                            CacheBuilder.newBuilder().build<Unit, Unit>()
                        } else {
                            DummyCache<Unit, Unit>()
                        }
                    )
                }

                given("cached value ${testCase.cachedValue}") {

                    beforeGroup {
                        testCase.cachedValue?.run {
                            cacheSpy.put(Unit, testCase.cachedValue)
                        }
                    }

                    given("an executor emitting ${testCase.executorOutput.toDescriptionString()}") {
                        val coordinator by memoized(mode = CachingMode.SCOPE) {
                            DataStreamCoordinator(
                                Unit,
                                mockExecutorFactory,
                                null,
                                cacheSpy,
                                autoLoadingStatus = true,
                                wrapUncaughtThrowables = true
                            )
                        }

                        beforeGroup {
                            whenever(mockExecutorFactory.invoke(Unit))
                                .thenReturn(
                                    testCase.executorOutput[0],
                                    *testCase.executorOutput.subList(1, testCase.executorOutput.size).toTypedArray()
                                )
                        }
                        given("first observer is subscribed via ${testCase.firstCommand}") {
                            var testObserver: TestObserver<ResourceStatus<Unit>>? = null
                            var firstObserverInitialValueCount = 0
                            beforeGroup {
                                testObserver = coordinator.responseStream.test()
                                coordinator.commandRelay.accept(testCase.firstCommand)
                                firstObserverInitialValueCount = testObserver!!.valueCount()
                            }

                            it("emits to the first subscriber: ${testCase.firstExpectedOutputInitial.joinToString(" ", "[", "]")}") {
                                println("Expected: ${testCase.firstExpectedOutputInitial.joinToString(" ", "[", "]")}")
                                println("Actual  : ${testObserver?.values()?.joinToString(" ", "[", "]")}")
                                testObserver?.values()?.run {
                                    assertThat(this).containsExactly(*testCase.firstExpectedOutputInitial.toTypedArray())
                                }
                            }

                            if (testCase.firstCommand.isInvalidationCommand()) {
                                it("clears the cached value") {
                                    verify(cacheSpy).invalidate(Unit)
                                }
                            }

                            given("second observer is subscribed via ${testCase.secondCommand}") {
                                var testObserver2: TestObserver<ResourceStatus<Unit>>? = null
                                beforeGroup {
                                    testObserver2 = coordinator.responseStream.test()
                                    coordinator.commandRelay.accept(testCase.secondCommand)
                                }

                                if (testCase.secondCommand.isInvalidationCommand()) {
                                    it("clears the cached value") {
                                        verify(cacheSpy).invalidate(Unit)
                                    }
                                }

                                it("emits to the second subscriber:  ${testCase.secondExpectedOutput.joinToString(" ", "[", "]")}") {
                                    println("Expected: ${testCase.secondExpectedOutput.joinToString(" ", "[", "]")}")
                                    println("Actual  : ${testObserver2?.values()?.joinToString(" ", "[", "]")}")
                                    testObserver2?.values()?.run {
                                        assertThat(this).containsExactly(*testCase.secondExpectedOutput.toTypedArray())
                                    }
                                }

                                it(
                                    "emits to the first subscriber:  ${testCase.firstExpectedOutputAfterSecondSubscription
                                        .joinToString(" ", "[", "]")}"
                                ) {
                                    val laterEmissions =
                                        testObserver?.values()?.subList(firstObserverInitialValueCount, testObserver!!.valueCount())
                                    println("Expected: ${testCase.firstExpectedOutputAfterSecondSubscription.joinToString(" ", "[", "]")}")
                                    println("Actual  : ${laterEmissions?.joinToString(" ", "[", "]")}")
                                    laterEmissions?.run {
                                        assertThat(this).containsExactly(*testCase.firstExpectedOutputAfterSecondSubscription.toTypedArray())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        afterGroup { RxJavaPlugins.reset() }
    }
})

fun RepositoryCommand.isInvalidationCommand() = this == RepositoryCommand.InvalidateWithCacheBlock || this == RepositoryCommand.Invalidate

fun List<ResourceStream<Unit>>.toDescriptionString(): String {
    return map {
        it.blockingIterable().joinToString(" ", "[", "]")
    }.reduce { acc, string -> "$acc $string" }
}
