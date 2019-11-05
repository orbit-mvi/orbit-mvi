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

import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.util.concurrent.CountDownLatch

internal class OrbitSpek : Spek({

    Feature("Orbit DSL syntax") {
        createTestMiddleware {

            perform("something")
                .on<Int>()
                .withReducer { currentState.copy(id = currentState.id + event) }

            perform("something else")
                .on<Int>()
                .loopBack { currentState.id + event }

            perform("something entirely else")
                .on<Int>()
                .sideEffect { println("${currentState.id + event}") }
                .transform { eventObservable.map { currentState.id + it + 2 } }
                .sideEffect { println("$event") }
                .sideEffect { post("$event") }
                .withReducer { State(currentState.id + event) }
        }
    }

    Feature("Orbit DSL tests") {

        Scenario("no flows") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

            Given("A middleware with no flows") {
                middleware = createTestMiddleware {}
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("connecting to the middleware") {
                testObserver = orbitContainer.orbit.test()
            }

            Then("emits the initial state") {
                testObserver.assertValueSequence(listOf(middleware.initialState))
            }
        }

        Scenario("a flow that reduces an action") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

            Given("A middleware with one reducer flow") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .withReducer { State(currentState.id + event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(State(42), State(47)))
            }
        }

        Scenario("a flow with a transformer and reducer") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

            Given("A middleware with a transformer and reducer") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform { eventObservable.map { it * 2 } }
                        .withReducer { State(currentState.id + event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(State(42), State(52)))
            }
        }

        Scenario("a flow with two transformers and a reducer") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

            Given("A middleware with two transformers and a reducer") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform { eventObservable.map { it * 2 } }
                        .transform { eventObservable.map { it * 2 } }
                        .withReducer { State(currentState.id + event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(State(42), State(62)))
            }
        }

        Scenario("a flow with two transformers that is ignored") {
            val latch = CountDownLatch(1)
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

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
                testObserver.assertValueSequence(listOf(State(42)))
            }
        }

        Scenario("a flow with a transformer loopback and a flow with a transformer and reducer") {
            data class IntModified(val value: Int)

            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

            Given("A middleware with a transformer loopback flow and transform/reduce flow") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform { eventObservable.map { it * 2 } }
                        .loopBack { IntModified(event) }

                    perform("something else")
                        .on<IntModified>()
                        .transform { eventObservable.map { it.value * 2 } }
                        .withReducer { State(currentState.id + event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(State(42), State(62)))
            }
        }

        Scenario("a flow with two transformers with reducers") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

            fun myReducer(event: Int): State {
                return State(event)
            }

            Given("A middleware with two transform/reduce flows") {
                middleware = createTestMiddleware {
                    perform("something")
                        .on<Int>()
                        .transform { eventObservable.map { it * 2 } }
                        .withReducer { myReducer(event) }

                    perform("something else")
                        .on<Int>()
                        .transform { eventObservable.map { it + 2 } }
                        .withReducer { State(event) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(5)
            }

            Then("produces a correct series of states") {
                testObserver.awaitCount(3)
                testObserver.assertValueSet(listOf(State(42), State(10), State(7)))
            }
        }
        Scenario("a flow with three transformers with reducers") {

            class One
            class Two
            class Three

            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>
            val expectedOutput = mutableListOf(State(0))

            Given("A middleware with three transform/reduce flows") {
                middleware = createTestMiddleware(State(0)) {
                    perform("one")
                        .on<One>()
                        .withReducer { State(1) }

                    perform("two")
                        .on<Two>()
                        .withReducer { State(2) }

                    perform("three")
                        .on<Three>()
                        .withReducer { State(3) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending actions") {
                testObserver = orbitContainer.orbit.test()
                for (i in 0 until 99) {
                    val value = (i % 3)
                    expectedOutput.add(State(value + 1))

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

        Scenario("posting side effects") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var sideEffects: TestObserver<String>

            Given("A middleware with a single post side effect as the first transformer") {
                middleware = createTestMiddleware(State(1)) {
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
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var sideEffects: TestObserver<String>
            val testSideEffectRelay = PublishSubject.create<String>()
            val testSideEffectObserver = testSideEffectRelay.test()

            Given("A middleware with a single side effect as the first transformer") {
                middleware = createTestMiddleware(State(1)) {
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

        Scenario("trying to build flows with the same description throw an exception") {
            lateinit var flows: OrbitsBuilder<State, String>.() -> Unit
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
})

private fun createTestMiddleware(
    initialState: State = State(42),
    block: OrbitsBuilder<State, String>.() -> Unit
) = middleware<State, String>(initialState) {
    this.apply(block)
}

private data class State(val id: Int)

private const val AWAIT_TIMEOUT = 10000L
