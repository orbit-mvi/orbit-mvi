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

package com.babylon.orbit

import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

internal class OrbitContainerThreadingSpek : Spek({

    beforeGroup {
        RxJavaPlugins.setIoSchedulerHandler {
            RxJavaPlugins.createIoScheduler {
                Thread(
                    it,
                    "IO"
                )
            }
        }
    }

    afterGroup {
        RxJavaPlugins.reset()
    }

    Feature("Container - Threading") {

        Scenario("Side effects execute on the current thread (before a transform - reducer thread)") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            val testSubject = PublishSubject.create<Int>()
            val testObserver = testSubject.test()
            lateinit var sideEffectThreadName: String

            Given("A middleware with a side effect") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .sideEffect {
                            sideEffectThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                orbitContainer.sendAction(5)
                testObserver.awaitCount(1)
            }

            Then("The side effect runs on the reducer thread") {
                assertThat(sideEffectThreadName).isEqualTo("reducerThread")
            }
        }

        Scenario("Reducers execute on reducer thread") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            val testSubject = PublishSubject.create<Int>()
            val testObserver = testSubject.test()
            lateinit var reducerThreadName: String

            Given("A middleware with a reducer") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .reduce {
                            reducerThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                            currentState
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                orbitContainer.sendAction(5)
                testObserver.awaitCount(1)
            }

            Then("The reducer runs on the reducer thread") {
                assertThat(reducerThreadName).isEqualTo("reducerThread")
            }
        }

        Scenario("Transformer executes on IO thread") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            val testSubject = PublishSubject.create<Int>()
            val testObserver = testSubject.test()
            lateinit var transformThreadName: String

            Given("A middleware with a transformer") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform {
                            eventObservable.map { it * 2 }
                                .doOnNext {
                                    transformThreadName = Thread.currentThread().name
                                    testSubject.onNext(it)
                                }
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                orbitContainer.sendAction(5)
                testObserver.awaitCount(1)
            }

            Then("The transformer runs on the IO thread") {
                assertThat(transformThreadName).isEqualTo("IO")
            }
        }

        Scenario("The downstream side effects of a transformer execute on IO thread") {

            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            val testSubject = PublishSubject.create<Int>()
            val testObserver = testSubject.test()
            lateinit var firstSideEffectThreadName: String
            lateinit var firstransformThreadName: String
            lateinit var secondSideEffectThreadName: String

            Given("A middleware with a mix of side effects and transformers") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .sideEffect {
                            firstSideEffectThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                        }
                        .transform {
                            eventObservable.map { it * 2 }
                                .doOnNext {
                                    firstransformThreadName = Thread.currentThread().name
                                    testSubject.onNext(it)
                                }
                        }
                        .sideEffect {
                            secondSideEffectThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                orbitContainer.sendAction(5)
                testObserver.awaitCount(3)
            }

            Then("The first side effect runs on the reducer thread") {
                assertThat(firstSideEffectThreadName).isEqualTo("reducerThread")
            }

            And("The first transformer runs on the IO thread") {
                assertThat(firstransformThreadName).isEqualTo("IO")
            }

            And("The second side effect runs on the IO thread") {
                assertThat(secondSideEffectThreadName).isEqualTo("IO")
            }
        }

        Scenario("The downstream transformers of a transformer execute on IO thread") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            val testSubject = PublishSubject.create<Int>()
            val testObserver = testSubject.test()
            lateinit var firstransformThreadName: String
            lateinit var secondTransformThreadName: String

            Given("A middleware with two transformers") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform {
                            eventObservable.map { it * 2 }
                                .doOnNext {
                                    firstransformThreadName = Thread.currentThread().name
                                    testSubject.onNext(it)
                                }
                        }
                        .transform {
                            eventObservable.map { it * 2 }
                                .doOnNext {
                                    secondTransformThreadName = Thread.currentThread().name
                                    testSubject.onNext(it)
                                }
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                orbitContainer.sendAction(5)
                testObserver.awaitCount(2)
            }

            Then("The first transformer runs on the IO thread") {
                assertThat(firstransformThreadName).isEqualTo("IO")
            }

            And("The second transformer runs on the IO thread") {
                assertThat(secondTransformThreadName).isEqualTo("IO")
            }
        }

        Scenario("The downstream reducers of a transformer execute on reducer thread") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            val testSubject = PublishSubject.create<Int>()
            val testObserver = testSubject.test()
            lateinit var firstReducerThreadName: String
            lateinit var firstransformThreadName: String
            lateinit var secondReducerThreadName: String

            Given("A middleware with a mix of reducers and transformers") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .reduce {
                            firstReducerThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                            currentState
                        }
                        .transform {
                            eventObservable.map { it * 2 }
                                .doOnNext {
                                    firstransformThreadName = Thread.currentThread().name
                                    testSubject.onNext(it)
                                }
                        }
                        .reduce {
                            secondReducerThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                            currentState
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                orbitContainer.sendAction(5)
                testObserver.awaitCount(3)
            }

            Then("The first reducer runs on the reducer thread") {
                assertThat(firstReducerThreadName).isEqualTo("reducerThread")
            }

            And("The first transformer runs on the IO thread") {
                assertThat(firstransformThreadName).isEqualTo("IO")
            }

            And("The second reducer runs on the reducer thread") {
                assertThat(secondReducerThreadName).isEqualTo("reducerThread")
            }
        }

        Scenario("The downstream side effect of a reducer executes on IO thread") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            val testSubject = PublishSubject.create<Int>()
            val testObserver = testSubject.test()
            lateinit var transformThreadName: String
            lateinit var firstReducerThreadName: String
            lateinit var secondReducerThreadName: String
            lateinit var sideEffectThreadName: String

            Given("A middleware with a mix of reducers and transformers") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform {
                            eventObservable.map { currentState.id * 2 }
                                .doOnNext {
                                    transformThreadName = Thread.currentThread().name
                                    testSubject.onNext(it)
                                }
                        }
                        .reduce {
                            firstReducerThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                            currentState.copy(currentState.id + 1)
                        }
                        .sideEffect {
                            sideEffectThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                        }
                        .reduce {
                            secondReducerThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                            currentState.copy(currentState.id + 1)
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                orbitContainer.sendAction(5)
                testObserver.awaitCount(3)
            }

            Then("The transformer runs on the io thread") {
                assertThat(transformThreadName).isEqualTo("IO")
            }

            And("The first reducer runs on the reducer thread") {
                assertThat(firstReducerThreadName).isEqualTo("reducerThread")
            }

            And("The side effect runs on the IO thread") {
                assertThat(sideEffectThreadName).isEqualTo("IO")
            }

            And("The second reducer runs on the reducer thread") {
                assertThat(secondReducerThreadName).isEqualTo("reducerThread")
            }
        }

        Scenario("Middleware in test mode executes actions in a blocking way") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            val testSubject = PublishSubject.create<Int>()
            val testObserver = testSubject.test()
            lateinit var firstReducerThreadName: String
            lateinit var firstransformThreadName: String
            lateinit var secondReducerThreadName: String
            var firstSideEffectTriggered = false
            var secondSideEffectTriggered = false
            lateinit var expectedThreadName: String

            Given("A test middleware with a mix of operators") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .reduce {
                            firstReducerThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                            currentState
                        }
                        .transform {
                            eventObservable.map { it * 2 }
                                .doOnNext {
                                    firstransformThreadName = Thread.currentThread().name
                                    testSubject.onNext(it)
                                }
                        }
                        .sideEffect {
                            firstSideEffectTriggered = true
                        }
                        .reduce {
                            secondReducerThreadName = Thread.currentThread().name
                            testSubject.onNext(event)
                            currentState
                        }
                        .sideEffect {
                            secondSideEffectTriggered = true
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware.test())
            }

            When("sending an action") {
                expectedThreadName = Thread.currentThread().name
                orbitContainer.sendAction(5)
//                testObserver.awaitCount(3)
            }

            Then("Three states are emitted") {
                assertThat(testObserver.valueCount()).isEqualTo(3)
            }
            And("The first reducer runs on the test thread") {
                assertThat(firstReducerThreadName).isEqualTo(expectedThreadName)
            }
            And("The first transformer runs on the test thread") {
                assertThat(firstransformThreadName).isEqualTo(expectedThreadName)
            }
            And("The second reducer runs on the test thread") {
                assertThat(secondReducerThreadName).isEqualTo(expectedThreadName)
            }
            And("The first side effectis triggered") {
                assertThat(firstSideEffectTriggered).isTrue()
            }
            And("The second side effectis triggered") {
                assertThat(secondSideEffectTriggered).isTrue()
            }
        }
    }
})
