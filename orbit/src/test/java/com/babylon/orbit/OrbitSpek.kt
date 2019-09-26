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

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.concurrent.CountDownLatch

internal class OrbitSpek : Spek({

    given("no flows") {
        val middleware by memoized {
            createTestMiddleware {
            }
        }
        val flowContainer by memoized { BaseOrbitContainer(middleware) }

        on("connecting to the middleware") {
            val emittedValues = flowContainer.orbit.test().values()

            it("emits the initial state") {
                assertThat(emittedValues).containsExactly(middleware.initialState)
            }
        }
    }

    given("a flow that reduces an action") {

        val latch by memoized { CountDownLatch(1) }

        val middleware by memoized {
            createTestMiddleware {
                perform("something")
                    .on<Int>()
                    .withReducer { currentState, event ->
                        State(currentState.id + event).also { latch.countDown() }
                    }
            }
        }
        val flowContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending an action") {
            val testObserver = flowContainer.orbit.test()
            flowContainer.inputRelay.accept(5)
            latch.await()

            it("produces a correct end state") {
                assertThat(testObserver.values()).containsExactly(State(42), State(47))
            }
        }
    }

    given("a flow that reduces an action using a simple reducer") {
        val latch by memoized { CountDownLatch(1) }

        val middleware by memoized {
            createTestMiddleware {
                perform("something")
                    .on<Int>()
                    .withReducer { currentState ->
                        State(currentState.id + 22).also { latch.countDown() }
                    }
            }
        }
        val flowContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending an action") {
            val testObserver = flowContainer.orbit.test()
            flowContainer.inputRelay.accept(5)
            latch.await()

            it("produces a correct end state") {
                assertThat(testObserver.values()).containsExactly(State(42), State(64))
            }
        }
    }

    given("a flow with a transformer and reducer") {
        val latch by memoized { CountDownLatch(1) }

        val middleware by memoized {
            createTestMiddleware {
                perform("something")
                    .on<Int>()
                    .transform { this.map { it.action * 2 } }
                    .withReducer { currentState, event ->
                        State(currentState.id + event).also { latch.countDown() }
                    }
            }
        }
        val flowContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending an action") {
            val testObserver = flowContainer.orbit.test()
            flowContainer.inputRelay.accept(5)
            latch.await()

            it("produces a correct end state") {
                assertThat(testObserver.values()).containsExactly(State(42), State(52))
            }
        }
    }

    given("a flow with a transformer and simple reducer") {
        val latch by memoized { CountDownLatch(1) }

        val middleware by memoized {
            createTestMiddleware {
                perform("something")
                    .on<Int>()
                    .transform { this.map { it.action * 2 } }
                    .withReducer { currentState ->
                        State(currentState.id + 22).also { latch.countDown() }
                    }
            }
        }
        val flowContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending an action") {
            val testObserver = flowContainer.orbit.test()
            flowContainer.inputRelay.accept(5)
            latch.await()

            it("produces a correct end state") {
                assertThat(testObserver.values()).containsExactly(State(42), State(64))
            }
        }
    }

    given("a flow with two transformers and a reducer") {
        val latch by memoized { CountDownLatch(1) }

        val middleware by memoized {
            createTestMiddleware {
                perform("something")
                    .on<Int>()
                    .transform { this.map { it.action * 2 } }
                    .transform { this.map { it * 2 } }
                    .withReducer { currentState, event ->
                        State(currentState.id + event).also { latch.countDown() }
                    }
            }
        }
        val flowContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending an action") {
            val testObserver = flowContainer.orbit.test()
            flowContainer.inputRelay.accept(5)
            latch.await()

            it("produces a correct end state") {
                assertThat(testObserver.values()).containsExactly(State(42), State(62))
            }
        }
    }

    given("a flow with two transformers that is ignored") {
        val latch by memoized { CountDownLatch(1) }

        val middleware by memoized {
            createTestMiddleware {
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
        }
        val flowContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending an action") {
            val testObserver = flowContainer.orbit.test()
            flowContainer.inputRelay.accept(5)
            latch.await()

            it("emits just the initial state after connecting") {
                assertThat(testObserver.values()).containsExactly(State(42))
            }
        }
    }

    given("a flow with a transformer loopback and a flow with a transformer and reducer") {
        data class IntModified(val value: Int)

        val latch by memoized { CountDownLatch(1) }

        val middleware by memoized {
            createTestMiddleware {
                perform("something")
                    .on<Int>()
                    .transform { this.map { it.action * 2 } }
                    .loopBack { IntModified(it) }

                perform("something")
                    .on<IntModified>()
                    .transform { this.map { it.action.value * 2 } }
                    .withReducer { currentState, event ->
                        State(currentState.id + event).also { latch.countDown() }
                    }
            }
        }
        val flowContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending an action") {
            val testObserver = flowContainer.orbit.test()
            flowContainer.inputRelay.accept(5)
            latch.await()

            it("produces a correct end state") {
                println(testObserver.values())
                assertThat(testObserver.values()).containsExactly(State(42), State(62))
            }
        }
    }

    given("a flow with two transformers with reducers") {
        val latch by memoized { CountDownLatch(2) }

        val middleware by memoized {
            createTestMiddleware {
                perform("something")
                    .on<Int>()
                    .transform { this.map { it.action * 2 } }
                    .withReducer { currentState, event ->
                        State(currentState.id + event).also { latch.countDown() }
                    }

                perform("something")
                    .on<Int>()
                    .transform { this.map { it.action + 2 } }
                    .withReducer { currentState, event ->
                        State(currentState.id + event).also { latch.countDown() }
                    }
            }
        }
        val flowContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending an action") {
            val testObserver = flowContainer.orbit.test()
            flowContainer.inputRelay.accept(5)
            latch.await()

            it("produces a correct series of states") {
                println(testObserver.values())
                assertThat(testObserver.values()).containsOnly(State(42), State(52), State(59))
            }
        }
    }
    given("a flow with two transformers with reducers") {

        class One
        class Two
        class Three

        val latch by memoized { CountDownLatch(99) }

        val middleware by memoized {
            createTestMiddleware(State(0)) {
                perform("one")
                    .on<One>()
                    .withReducer { _, _ ->
                        println("one ${Thread.currentThread().name}")
                        State(1).also { latch.countDown() }
                    }

                perform("two")
                    .on<Two>()
                    .withReducer { _, _ ->
                        println("two ${Thread.currentThread().name}")
                        State(2).also { latch.countDown() }
                    }

                perform("three")
                    .on<Three>()
                    .withReducer { _, _ ->
                        println("three ${Thread.currentThread().name}")
                        State(3).also { latch.countDown() }
                    }
            }
        }
        val orbitContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending actions") {
            val testObserver = orbitContainer.orbit.test()
            val expectedOutput = mutableListOf(State(0))
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

            it("produces a correct series of states") {
                latch.await()
                println(testObserver.values())
                println(expectedOutput)
                assertThat(testObserver.values()).isEqualTo(expectedOutput)
            }
        }
    }

    given("a flow with side effects") {

        val latch by memoized { CountDownLatch(1) }

        val middleware by memoized {
            createTestMiddleware(State(1)) {
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
                        latch.countDown()
                    }
                    .ignoringEvents()
            }
        }
        val orbitContainer by memoized { BaseOrbitContainer(middleware) }

        on("sending actions") {
            val testObserver = orbitContainer.orbit.test()
            val testSideEffectObserver = orbitContainer.sideEffect.test()

            orbitContainer.inputRelay.accept(Unit)

            it("produces a correct series of states") {
                latch.await()
                assertThat(testObserver.values()).containsExactly(State(1))
            }

            it("produces a correct series of side effects") {
                latch.await()
                assertThat(testSideEffectObserver.values()).containsExactly("1", "2", "three")
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
