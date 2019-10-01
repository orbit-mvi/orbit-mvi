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
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.util.concurrent.CountDownLatch

internal class OrbitSpek : Spek({
    Feature("Orbit DSL") {

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
                            .withReducer { currentState, event ->
                                State(currentState.id + event)
                            }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.inputRelay.accept(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(State(42), State(47)))
            }
        }

        Scenario("a flow that reduces an action using a simple reducer") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

            Given("A middleware with a simple reducer flow") {
                middleware = createTestMiddleware {
                    perform("something")
                            .on<Int>()
                            .withReducer { currentState ->
                                State(currentState.id + 22)
                            }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.inputRelay.accept(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(State(42), State(64)))
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
                            .transform { this.map { it.action * 2 } }
                            .withReducer { currentState, event ->
                                State(currentState.id + event)
                            }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.inputRelay.accept(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(State(42), State(52)))
            }
        }

        Scenario("a flow with a transformer and simple reducer") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

            Given("A middleware with a transformer and simple reducer") {
                middleware = createTestMiddleware {
                    perform("something")
                            .on<Int>()
                            .transform { this.map { it.action * 2 } }
                            .withReducer { currentState ->
                                State(currentState.id + 22)
                            }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.inputRelay.accept(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                testObserver.assertValueSequence(listOf(State(42), State(64)))
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
                            .transform { this.map { it.action * 2 } }
                            .transform { this.map { it * 2 } }
                            .withReducer { currentState, event ->
                                State(currentState.id + event)
                            }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.inputRelay.accept(5)
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
                            .transform { this.map { it.action * 2 } }
                            .transform { this.map { it * 2 } }
                            .ignoringEvents()

                    perform("unlatch")
                            .on<Int>()
                            .transform {
                                latch.countDown()
                                this
                            }
                            .ignoringEvents()
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.inputRelay.accept(5)
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
                            .transform { this.map { it.action * 2 } }
                            .loopBack { IntModified(it) }

                    perform("something")
                            .on<IntModified>()
                            .transform { this.map { it.action.value * 2 } }
                            .withReducer { currentState, event ->
                                State(currentState.id + event)
                            }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.inputRelay.accept(5)
            }

            Then("produces a correct end state") {
                testObserver.awaitCount(2)
                println(testObserver.values())
                testObserver.assertValueSequence(listOf(State(42), State(62)))
            }
        }

        Scenario("a flow with two transformers with reducers") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>

            Given("A middleware with two transform/reduce flows") {
                middleware = createTestMiddleware {
                    perform("something")
                            .on<Int>()
                            .transform { this.map { it.action * 2 } }
                            .withReducer { _, event ->
                                State(event)
                            }

                    perform("something")
                            .on<Int>()
                            .transform { this.map { it.action + 2 } }
                            .withReducer { _, event ->
                                State(event)
                            }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending an action") {
                testObserver = orbitContainer.orbit.test()
                orbitContainer.inputRelay.accept(5)
            }

            Then("produces a correct series of states") {
                testObserver.awaitCount(3)
                println(testObserver)
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
                            .withReducer { _, _ ->
                                println("one ${Thread.currentThread().name}")
                                State(1)
                            }

                    perform("two")
                            .on<Two>()
                            .withReducer { _, _ ->
                                println("two ${Thread.currentThread().name}")
                                State(2)
                            }

                    perform("three")
                            .on<Three>()
                            .withReducer { _, _ ->
                                println("three ${Thread.currentThread().name}")
                                State(3)
                            }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending actions") {
                testObserver = orbitContainer.orbit.test()
                for (i in 0 until 99) {
                    val value = (i % 3)
                    expectedOutput.add(State(value + 1))

                    orbitContainer.inputRelay.accept(
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
                println(testObserver.values())
                println(expectedOutput)
                testObserver.assertValueSequence(expectedOutput)
            }
        }

        Scenario("a flow with side effects") {
            lateinit var middleware: Middleware<State, String>
            lateinit var orbitContainer: BaseOrbitContainer<State, String>
            lateinit var testObserver: TestObserver<State>
            lateinit var sideEffects: TestObserver<String>

            Given("A middleware with multiple side effects within one flow") {
                middleware = createTestMiddleware(State(1)) {
                    perform("something")
                            .on<Unit>()
                            .sideEffect { relay, actionState ->
                                relay.post(actionState.inputState.id.toString())
                            }
                            .transform {
                                map {
                                    it.inputState.id + 1
                                }
                            }
                            .sideEffect { relay, id ->
                                relay.post(id.toString())
                            }
                            .transform {
                                map {
                                    "three"
                                }
                            }
                            .sideEffect { relay, string ->
                                relay.post(string)
                            }
                            .withReducer { state -> state.copy(id = state.id + 1) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("sending actions") {
                testObserver = orbitContainer.orbit.test()
                sideEffects = orbitContainer.sideEffect.test()

                orbitContainer.inputRelay.accept(Unit)

                testObserver.awaitCount(2)
            }

            Then("produces a correct series of states") {
                testObserver.assertValueSequence(listOf(State(1), State(2)))
            }

            Then("produces a correct series of side effects") {
                sideEffects.assertValueSequence(listOf("1", "2", "three"))
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
