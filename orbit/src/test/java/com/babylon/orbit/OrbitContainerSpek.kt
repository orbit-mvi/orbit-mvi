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
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

internal class OrbitContainerSpek : Spek({
    Feature("Container - State") {
        Scenario("Initial state is always emitted") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver: TestObserver<TestState>

            Given("A middleware with no flows") {
                middleware = createTestMiddleware {}
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I connect to the middleware") {
                testObserver = orbitContainer.orbit.test()
            }

            Then("emits the initial state") {
                testObserver.assertValueSequence(listOf(middleware.initialState))
            }
        }

        Scenario("Current state always emitted upon subscription") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver1: TestObserver<TestState>
            lateinit var testObserver2: TestObserver<TestState>

            Given("A middleware with no flows") {
                middleware = createTestMiddleware {}
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I connect observer 1 to the middleware") {
                testObserver1 = orbitContainer.orbit.test()
                testObserver1.awaitCount(1)
            }

            And("I connect observer 2 to the middleware") {
                testObserver2 = orbitContainer.orbit.test()
                testObserver1.awaitCount(1)
            }

            Then("Observer 1 gets the initial state") {
                testObserver1.assertValueSequence(listOf(middleware.initialState))
            }

            And("Observer 2 gets the initial state") {
                testObserver2.assertValueSequence(listOf(middleware.initialState))
            }
        }

        Scenario("Updated state is emitted on connection after it changes while disconnected") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver1: TestObserver<TestState>
            lateinit var testObserver2: TestObserver<TestState>

            Given("A middleware with a reducer") {
                middleware = createTestMiddleware {
                    perform("increment id")
                        .on<Unit>()
                        .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I send an increment event to the flow") {
                val awaitObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(Unit)
                awaitObserver.awaitCount(2)
                awaitObserver.dispose()
            }

            And("I connect observer 1 to the middleware") {
                testObserver1 = orbitContainer.orbit.test()
                testObserver1.awaitCount(1)
            }

            And("I connect observer 2 to the middleware") {
                testObserver2 = orbitContainer.orbit.test()
                testObserver1.awaitCount(1)
            }

            Then("Observer 1 gets the modified state") {
                testObserver1.assertValueSequence(listOf(TestState(43)))
            }

            And("Observer 2 gets the modified state") {
                testObserver2.assertValueSequence(listOf(TestState(43)))
            }
        }

        Scenario("Current state can be queried directly before and after modification") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var state1: TestState
            lateinit var state2: TestState

            Given("A middleware with a reducer") {
                middleware = createTestMiddleware {
                    perform("increment id")
                        .on<Unit>()
                        .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I query the state") {
                state1 = orbitContainer.currentState
            }

            And("I send an increment event to the flow") {
                val awaitObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(Unit)
                awaitObserver.awaitCount(2)
                awaitObserver.dispose()
            }

            And("I query the state") {
                state2 = orbitContainer.currentState
            }

            Then("State 1 is the initial state") {
                assertThat(state1).isEqualTo(TestState(42))
            }

            And("State 2 is the modified state") {
                assertThat(state2).isEqualTo(TestState(43))
            }
        }
    }

    Feature("Container - Cached Side Effects") {
        Scenario("Side effects are multicast to all current observers by default") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver1: TestObserver<String>
            lateinit var testObserver2: TestObserver<String>

            Given("A middleware with side effect") {
                middleware = createTestMiddleware {
                    perform("send side effect")
                        .on<Unit>()
                        .sideEffect { post("foobar") }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I connect observer 1 to the middleware") {
                testObserver1 = orbitContainer.sideEffect.test()
            }

            And("I connect observer 2 to the middleware") {
                testObserver2 = orbitContainer.sideEffect.test()
            }

            And("I send an event to the container") {
                orbitContainer.sendAction(Unit)
                testObserver1.awaitCount(1)
                testObserver2.awaitCount(1)
            }

            Then("Observer 1 gets the side effect") {
                testObserver1.assertValueSequence(listOf("foobar"))
            }

            And("Observer 2 gets the side effect") {
                testObserver2.assertValueSequence(listOf("foobar"))
            }
        }

        Scenario("Side effects are cached while there is no connected observer by default") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var stateObserver: TestObserver<TestState>
            lateinit var sideEffectObserver: TestObserver<String>

            Given("A middleware with side effects") {
                middleware = createTestMiddleware {
                    perform("send side effect")
                        .on<Unit>()
                        .sideEffect { post("foo") }
                        .sideEffect { post("bar") }
                        .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I send an event to the container") {
                stateObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(Unit)
                stateObserver.awaitCount(2)
            }

            And("I connect the side effect observer to the middleware") {
                sideEffectObserver = orbitContainer.sideEffect.test()
                sideEffectObserver.awaitCount(2)
            }

            Then("The observer gets the side effect") {
                sideEffectObserver.assertValueSequence(listOf("foo", "bar"))
            }
        }

        Scenario("If I connect, disconnect and reconnect the side effects behave correctly by default") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var stateObserver: TestObserver<TestState>
            lateinit var sideEffectObserver: TestObserver<String>

            Given("A middleware with side effects") {
                middleware = createTestMiddleware {
                    perform("send side effect")
                        .on<Unit>()
                        .sideEffect { post("foo") }
                        .sideEffect { post("bar") }
                        .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I send an event to the container") {
                stateObserver = orbitContainer.orbit.test()
                sideEffectObserver = orbitContainer.sideEffect.test()
                orbitContainer.sendAction(Unit)
                stateObserver.awaitCount(2)
                sideEffectObserver.awaitCount(2)
            }

            Then("The observer gets the side effects") {
                sideEffectObserver.assertValueSequence(listOf("foo", "bar"))
            }

            When("I disconnect") {
                stateObserver.dispose()
                sideEffectObserver.dispose()
            }

            And("I send an event to the container") {
                orbitContainer.sendAction(Unit)
            }

            And("I resubscribe") {
                stateObserver = orbitContainer.orbit.test()
                sideEffectObserver = orbitContainer.sideEffect.test()
                stateObserver.awaitCount(1)
                sideEffectObserver.awaitCount(2)
            }

            Then("The new observer gets the side effects") {
                sideEffectObserver.assertValueSequence(listOf("foo", "bar"))
            }

            When("I send another event") {
                orbitContainer.sendAction(Unit)
                stateObserver.awaitCount(2)
                sideEffectObserver.awaitCount(4)
            }

            Then("The observer gets the side effects again") {
                sideEffectObserver.assertValueSequence(listOf("foo", "bar", "foo", "bar"))
            }
        }

        Scenario(
            "If I connect, disconnect and reconnect the side effects behave correctly" +
                    " when explicitly set to true"
        ) {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var stateObserver: TestObserver<TestState>
            lateinit var sideEffectObserver: TestObserver<String>

            Given("A middleware with side effects") {
                middleware = createTestMiddleware {
                    configuration {
                        sideEffectCachingEnabled = true
                    }
                    perform("send side effect")
                        .on<Unit>()
                        .sideEffect { post("foo") }
                        .sideEffect { post("bar") }
                        .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I send an event to the container") {
                stateObserver = orbitContainer.orbit.test()
                sideEffectObserver = orbitContainer.sideEffect.test()
                orbitContainer.sendAction(Unit)
                stateObserver.awaitCount(2)
                sideEffectObserver.awaitCount(2)
            }

            Then("The observer gets the side effects") {
                sideEffectObserver.assertValueSequence(listOf("foo", "bar"))
            }

            When("I disconnect") {
                stateObserver.dispose()
                sideEffectObserver.dispose()
            }

            And("I send an event to the container") {
                orbitContainer.sendAction(Unit)
            }

            And("I resubscribe") {
                stateObserver = orbitContainer.orbit.test()
                sideEffectObserver = orbitContainer.sideEffect.test()
                stateObserver.awaitCount(1)
                sideEffectObserver.awaitCount(2)
            }

            Then("The new observer gets the side effects") {
                sideEffectObserver.assertValueSequence(listOf("foo", "bar"))
            }

            When("I send another event") {
                orbitContainer.sendAction(Unit)
                stateObserver.awaitCount(2)
                sideEffectObserver.awaitCount(4)
            }

            Then("The observer gets the side effects again") {
                sideEffectObserver.assertValueSequence(listOf("foo", "bar", "foo", "bar"))
            }
        }

        Scenario("Cached side effects are guaranteed to be delivered to the first observer by default") {

            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var stateObserver: TestObserver<TestState>
            lateinit var sideEffectObserver1: TestObserver<String>
            lateinit var sideEffectObserver2: TestObserver<String>

            Given("A middleware with side effects") {
                middleware = createTestMiddleware {
                    perform("send side effect")
                        .on<Unit>()
                        .sideEffect { post("foo") }
                        .sideEffect { post("bar") }
                        .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I send an event to the container") {
                stateObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(Unit)
                stateObserver.awaitCount(2)
            }

            And("I connect both side effect observers to the middleware") {
                sideEffectObserver1 = orbitContainer.sideEffect.test()
                sideEffectObserver2 = orbitContainer.sideEffect.test()
                sideEffectObserver1.awaitCount(2)
            }

            Then("The first observer gets all cached side effects") {
                sideEffectObserver1.assertValueSequence(listOf("foo", "bar"))
            }

            And("The second observer is not guaranteed to get any side effects") {
                assertThat(sideEffectObserver2.values()).doesNotContainSequence(sideEffectObserver1.values())
            }
        }
    }

    Feature("Container - uncached Side Effects") {
        Scenario("Side effects are multicast to all current observers") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var testObserver1: TestObserver<String>
            lateinit var testObserver2: TestObserver<String>

            Given("A middleware with side effect") {
                middleware = createTestMiddleware {
                    configuration {
                        sideEffectCachingEnabled = false
                    }
                    perform("send side effect")
                        .on<Unit>()
                        .sideEffect { post("foobar") }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I connect observer 1 to the middleware") {
                testObserver1 = orbitContainer.sideEffect.test()
            }

            And("I connect observer 2 to the middleware") {
                testObserver2 = orbitContainer.sideEffect.test()
            }

            And("I send an event to the container") {
                orbitContainer.sendAction(Unit)
                testObserver1.awaitCount(1)
                testObserver2.awaitCount(1)
            }

            Then("Observer 1 gets the side effect") {
                testObserver1.assertValueSequence(listOf("foobar"))
            }

            And("Observer 2 gets the side effect") {
                testObserver2.assertValueSequence(listOf("foobar"))
            }
        }

        Scenario("Side effects are not cached while there is no connected observer") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: BaseOrbitContainer<TestState, String>
            lateinit var stateObserver: TestObserver<TestState>
            lateinit var sideEffectObserver: TestObserver<String>

            Given("A middleware with side effects") {
                middleware = createTestMiddleware {
                    configuration {
                        sideEffectCachingEnabled = false
                    }
                    perform("send side effect")
                        .on<Unit>()
                        .sideEffect { post("foo") }
                        .sideEffect { post("bar") }
                        .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
                }
                orbitContainer = BaseOrbitContainer(middleware)
            }

            When("I send an event to the container") {
                stateObserver = orbitContainer.orbit.test()
                orbitContainer.sendAction(Unit)
                stateObserver.awaitCount(2)
            }

            And("I connect the side effect observer to the middleware") {
                sideEffectObserver = orbitContainer.sideEffect.test()
                sideEffectObserver.awaitCount(2)
            }

            Then("The observer does not get the side effects") {
                sideEffectObserver.assertNoValues()
            }
        }
    }

    Feature("Container - Lifecycle") {
        Scenario("Lifecycle action sent on container creation") {
            lateinit var middleware: Middleware<TestState, String>
            val sideEffectSubject = PublishSubject.create<String>()
            val sideEffectTestObserver = sideEffectSubject.test()

            Given("A middleware with a side effect off a LifecycleEvent.Created") {
                middleware = createTestMiddleware {
                    perform("check lifecycle action")
                        .on<LifecycleAction.Created>()
                        .sideEffect { sideEffectSubject.onNext("foo") }
                }
                BaseOrbitContainer(middleware)
            }

            When("I connect to the middleware") {
                sideEffectTestObserver.awaitCount(1)
            }

            Then("The side effect is received") {
                sideEffectTestObserver.assertValue("foo")
            }
        }
    }
})
