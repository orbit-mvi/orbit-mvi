/*
 * Copyright 2020 Babylon Partners Limited
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

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.util.concurrent.CountDownLatch
import kotlin.random.Random

internal class DslSpek : Spek({

    Feature("DSL - syntax") {
        createTestMiddleware {

            configuration {
                sideEffectCachingEnabled = true
            }

            perform("something")
                .on<Int>()
                .reduce { currentState.copy(id = currentState.id + event) }

            perform("something else")
                .on<Int>()
                .loopBack { currentState.id + event }

            perform("something entirely else")
                .on<Int>()
                .sideEffect { println("${currentState.id + event}") }
                .transform { eventObservable.map { currentState.id + it + 2 } }
                .sideEffect { println("$event") }
                .sideEffect { post("$event") }
                .reduce { TestState(currentState.id + event) }
                .transform { eventObservable.map { it + 2 } }
        }

        Scenario("trying to build flows with the same description throw an exception") {
            lateinit var flows: OrbitsBuilder<TestState, String>.() -> Unit
            lateinit var throwable: Throwable

            Given("Flows with duplicate flow descriptions") {
                flows = {
                    perform("something")
                        .on<Int>()

                    perform("something")
                        .on<String>()
                }
            }

            When("we try to build a middleware using them") {
                throwable = Assertions.catchThrowable { createTestMiddleware(block = flows) }
            }

            Then("it throws an exception") {
                assertThat(throwable)
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessageContaining("something")
            }
        }
    }

    Feature("DSL - configuration") {
        Scenario("Set the caching to false and build the middleware") {
            lateinit var middleware: Middleware<TestState, String>
            var sideEffectCaching = true

            Given("middleware configured to disable side effect caching") {
                middleware = createTestMiddleware {
                    configuration {
                        sideEffectCachingEnabled = false
                    }
                }
            }

            When("I query the configuration for side effect caching") {
                sideEffectCaching = middleware.configuration.sideEffectCachingEnabled
            }

            Then("Side effect caching should be disabled") {
                assertThat(sideEffectCaching).isFalse()
            }
        }

        Scenario("Set the caching to true and build the middleware") {
            lateinit var middleware: Middleware<TestState, String>
            var sideEffectCaching = false

            Given("middleware configured to disable side effect caching") {
                middleware = createTestMiddleware {
                    configuration {
                        sideEffectCachingEnabled = true
                    }
                }
            }

            When("I query the configuration for side effect caching") {
                sideEffectCaching = middleware.configuration.sideEffectCachingEnabled
            }

            Then("Side effect caching should be enabled") {
                assertThat(sideEffectCaching).isTrue()
            }
        }
    }

    Feature("DSL - side effects") {
        Scenario("posting side effects") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var sideEffects: TestObserver<String>

            Given("A middleware with a single post side effect as the first transformer") {
                middleware = createTestMiddleware(TestState(1)) {
                    perform("something")
                        .on<Int>()
                        .sideEffect { post("${currentState.id + event}") }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending actions") {
                sideEffects = orbitContainer.sideEffect.test()

                orbitContainer.sendAction(5)

                sideEffects.awaitCount(1)
            }

            Then("posts a correct series of side effects") {
                sideEffects.assertValueSequence(listOf("6"))
            }
        }

        Scenario("non-posting side effects") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var sideEffects: TestObserver<String>
            val testSideEffectRelay = PublishSubject.create<String>()
            val testSideEffectObserver = testSideEffectRelay.test()

            Given("A middleware with a single side effect as the first transformer") {
                middleware = createTestMiddleware(TestState(1)) {
                    perform("something")
                        .on<Int>()
                        .sideEffect { testSideEffectRelay.onNext("${currentState.id + event}") }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending actions") {
                sideEffects = orbitContainer.sideEffect.test()

                orbitContainer.sendAction(5)

                testSideEffectObserver.awaitCount(1)
            }

            Then("it posts no side effects") {
                sideEffects.assertNoValues()
            }

            And("the side effect is executed") {
                testSideEffectObserver.assertValue("6")
            }
        }
    }
    Feature("DSL - transformers and reducers") {

        Scenario("a flow that reduces an action") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A middleware with one reducer flow") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .reduce { TestState(currentState.id + event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(TestState(42), TestState(47)))
            }
        }

        Scenario("a flow with a transformer and reducer") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A middleware with a transformer and reducer") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform { eventObservable.map { it * 2 } }
                        .reduce { TestState(currentState.id + event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(TestState(42), TestState(52)))
            }
        }
//          TODO to be uncommented when making reducers emit the reduced state
//        Scenario("side effects downstream of a reducer get the reduced state") {
//            lateinit var middleware: Middleware<TestState, String>
//            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
//            val testRelay = PublishSubject.create<TestState>()
//            val testObserver = testRelay.test()
//
//            Given("A middleware with a transformer and reducer") {
//                middleware = createTestMiddleware {
//                    perform("something")
//                        .on<Int>()
//                        .transform { eventObservable.map { it * 2 } }
//                        .reduce { TestState(currentState.id + event) }
//                        .sideEffect {
//                            testRelay.onNext(event)
//                        }
//                }
//                orbitContainer = BaseOrbitContainer(middleware)
//            }
//
//            When("sending an action") {
//                repeat((1..100).count()) {
//                    orbitContainer.sendAction(1)
//                }
//            }
//
//            Then("The states captured by the side effect are correct") {
//                testObserver.awaitCount(100)
//                val expectedStates = mutableListOf<TestState>()
//                repeat(100) {
//                    expectedStates.add(TestState(42 + (it + 1) * 2))
//                }
//                testObserver.assertValueSequence(expectedStates)
//            }
//        }
//
//        Scenario("transformers downstream of a reducer get the reduced state") {
//            lateinit var middleware: Middleware<TestState, String>
//            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
//            val testRelay = PublishSubject.create<TestState>()
//            val testObserver = testRelay.test()
//
//            Given("A middleware with a transformer and reducer") {
//                middleware = createTestMiddleware {
//                    perform("something")
//                        .on<Int>()
//                        .transform { eventObservable.map { it * 2 } }
//                        .reduce { TestState(currentState.id + event) }
//                        .transform {
//                            eventObservable.doOnNext {
//                                testRelay.onNext(it)
//                            }
//                        }
//                }
//                orbitContainer = BaseOrbitContainer(middleware)
//            }
//
//            When("sending an action") {
//                repeat((1..100).count()) {
//                    orbitContainer.sendAction(1)
//                }
//            }
//
//            Then("The states captured by the side effect are correct") {
//                testObserver.awaitCount(100)
//                val expectedStates = mutableListOf<TestState>()
//                repeat(100) {
//                    expectedStates.add(TestState(42 + (it + 1) * 2))
//                }
//                testObserver.assertValueSequence(expectedStates)
//            }
//        }
//
//        Scenario("loopbacks downstream of a reducer get the reduced state") {
//            lateinit var middleware: Middleware<TestState, String>
//            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
//            val testRelay = PublishSubject.create<TestState>()
//            val testObserver = testRelay.test()
//
//            Given("A middleware with a transformer and reducer") {
//                middleware = createTestMiddleware {
//                    perform("something")
//                        .on<Int>()
//                        .transform { eventObservable.map { it * 2 } }
//                        .reduce { TestState(currentState.id + event) }
//                        .loopBack {
//                            testRelay.onNext(event)
//                        }
//                }
//                orbitContainer = BaseOrbitContainer(middleware)
//            }
//
//            When("sending an action") {
//                repeat((1..100).count()) {
//                    orbitContainer.sendAction(1)
//                }
//            }
//
//            Then("The states captured by the side effect are correct") {
//                testObserver.awaitCount(100)
//                val expectedStates = mutableListOf<TestState>()
//                repeat(100) {
//                    expectedStates.add(TestState(42 + (it + 1) * 2))
//                }
//                testObserver.assertValueSequence(expectedStates)
//            }
//        }

        Scenario("a flow with two transformers and a reducer") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A middleware with two transformers and a reducer") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform { eventObservable.map { it * 2 } }
                        .transform { eventObservable.map { it * 2 } }
                        .reduce { TestState(currentState.id + event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(TestState(42), TestState(62)))
            }
        }

        Scenario("a flow with two transformers and no reducer") {
            val latch = CountDownLatch(1)
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A middleware with two transformer flows") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform { eventObservable.map { it * 2 } }
                        .transform { eventObservable.map { it * 2 } }

                    perform("unlatch")
                        .on<Int>()
                        .transform {
                            latch.countDown()
                            eventObservable
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
                latch.await()
            }

            Then("emits just the initial state after connecting") {
                testObserver.assertValueSequence(listOf(TestState(42)))
            }
        }

        Scenario("a flow with a transformer loopback and a flow with a transformer and reducer") {
            data class IntModified(val value: Int)

            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A middleware with a transformer loopback flow and transform/reduce flow") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform { eventObservable.map { it * 2 } }
                        .loopBack { IntModified(event) }

                    perform("something else")
                        .on<IntModified>()
                        .transform { eventObservable.map { it.value * 2 } }
                        .reduce { TestState(currentState.id + event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(TestState(42), TestState(62)))
            }
        }

        Scenario("a flow with two transformers with reducers") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            fun myReducer(event: Int): TestState {
                return TestState(event)
            }

            Given("A middleware with two transform/reduce flows") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform { eventObservable.map { it * 2 } }
                        .reduce { myReducer(event) }

                    perform("something else")
                        .on<Int>()
                        .transform { eventObservable.map { it + 2 } }
                        .reduce { TestState(event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct series of states") {
                testObserver.awaitCount(3)
                testObserver.assertValueSet(listOf(TestState(42), TestState(10), TestState(7)))
            }
        }
        Scenario("three flows with reducers reduce sequentially") {

            class One
            class Two
            class Three

            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>
            val expectedOutput = mutableListOf(TestState(0))

            Given("A middleware with three transform/reduce flows") {
                middleware = createTestMiddleware(TestState(0)) {
                    perform("one")
                        .on<One>()
                        .reduce { TestState(1) }

                    perform("two")
                        .on<Two>()
                        .reduce { TestState(2) }

                    perform("three")
                        .on<Three>()
                        .reduce { TestState(3) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending actions") {
                testObserver = orbitContainer.orbit.test()
                for (i in 0 until 99) {
                    val value = (i % 3)
                    expectedOutput.add(TestState(value + 1))

                    orbitContainer.sendAction(
                        when (value) {
                            0 -> One()
                            1 -> Two()
                            2 -> Three()
                            else -> throw IllegalStateException("misconfigured test")
                        }
                    )
                }
            }

            Then("produces a correct series of states") {
                testObserver.awaitCount(100)
                testObserver.assertValueSequence(expectedOutput)
            }
        }

        Scenario("All reductions get processed in order") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A connected middleware with a reducer") {
                middleware = createTestMiddleware(TestState(0)) {
                    perform("increment id")
                        .on<Int>()
                        .reduce { currentState.copy(id = event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
                testObserver = orbitContainer.orbit.test()
            }

            When("I send a series of events to the middleware") {
                for (i in 1..99) {
                    orbitContainer.sendAction(i)
                }
            }

            Then("I expect a correct sequence of reduced states") {
                testObserver.awaitCount(100)
                testObserver.assertValueSequence((0..99).map { TestState(it) })
            }
        }

        Scenario("No reductions from transformers get lost") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A connected middleware with a reducer") {
                val random = Random
                middleware = createTestMiddleware(TestState(0)) {
                    perform("increment id")
                        .on<Int>()
                        .transform {
                            eventObservable.doOnNext {
                                // do nothing, but force this to run on a separate thread
                            }
                        }
                        .reduce {
                            Thread.sleep(random.nextLong(20)) // simulate variable time to process
                            currentState.copy(id = event)
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
                testObserver = orbitContainer.orbit.test()
            }

            When("I send a series of events to the middleware") {
                for (i in 1..99) {
                    orbitContainer.sendAction(i)
                }
            }

            Then("I expect a correct sequence of reduced states") {
                testObserver.awaitCount(100)
                testObserver.assertValueSequence((0..99).map { TestState(it) })
            }
        }

        Scenario("No reductions from chained transformers and reducers get lost") {
            // This test verifies chaining
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A connected middleware with a reducer") {
                val random = Random
                middleware = createTestMiddleware(TestState(0)) {
                    perform("increment id")
                        .on<Int>()
                        .transform {
                            eventObservable.flatMap {
                                Observable.fromArray(*(0..99).toList().toTypedArray())
                            }
                        }
                        .reduce {
                            Thread.sleep(random.nextLong(10)) // simulate variable time to process
                            currentState.copy(id = event)
                        }
                        .transform {
                            eventObservable.flatMapIterable {
                                listOf(it * 111, it * 1111)
                            }
                        }
                        .reduce {
                            currentState.copy(id = event)
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware)
                testObserver = orbitContainer.orbit.test()
            }

            When("I send a series of events to the middleware") {
                orbitContainer.sendAction(0)
            }

            Then("I expect a correct sequence of reduced states", 20000L) {
                testObserver.awaitCount(300)
                assertThat(testObserver.values()).containsAll((0..99).flatMap {
                    listOf(
                        TestState(it),
                        TestState(it * 111),
                        TestState(it * 1111)
                    )
                })
            }
        }
    }

    Feature("Middleware in test mode") {
        Scenario("Middleware in test mode allows you to isolate flows") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A test middleware isolated to flow one") {
                middleware = createTestMiddleware {
                    perform("flow one")
                        .on<Int>()
                        .reduce {
                            currentState.copy(id = currentState.id + 11)
                        }
                    perform("flow two")
                        .on<Int>()
                        .reduce {
                            currentState.copy(id = currentState.id + 3)
                        }
                }
                orbitContainer = BaseOrbitContainer(middleware.test("flow one"))
                testObserver = orbitContainer.orbit.test()
            }

            When("sending an action") {
                orbitContainer.sendAction(5)
            }

            Then("Only two states are emitted") {
                testObserver.assertValuesOnly(TestState(42), TestState(53))
            }
        }
    }
})
