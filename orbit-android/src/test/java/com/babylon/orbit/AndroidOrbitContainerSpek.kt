package com.babylon.orbit

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

class AndroidOrbitContainerSpek : Spek({

    beforeGroup {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        RxAndroidPlugins.setMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
    }

    afterGroup {
        RxJavaPlugins.reset()
    }

    Feature("Android Container - Threading") {
        Scenario("Side effects and state updates are received on the android main thread") {
            lateinit var middleware: Middleware<TestState, String>
            lateinit var orbitContainer: AndroidOrbitContainer<TestState, String>
            lateinit var stateObserver: TestObserver<TestState>
            lateinit var sideEffectObserver: TestObserver<String>

            Given("An android container with a simple middleware") {
                middleware = createTestMiddleware {
                    perform("send side effect")
                        .on<Unit>()
                        .sideEffect { post("foo") }
                        .sideEffect { post("bar") }
                        .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
                }
                orbitContainer = AndroidOrbitContainer(middleware)
            }

            When("I send an event to the container") {
                stateObserver = orbitContainer.orbit.test()
                sideEffectObserver = orbitContainer.sideEffect.test()
                orbitContainer.sendAction(Unit)
                stateObserver.awaitCount(2)
            }

            Then("The state observer listens on the android main thread") {
                assertThat(stateObserver.lastThread().name).isEqualTo("main")
            }

            And("The side effect observer listens on the android main thread") {
                assertThat(sideEffectObserver.lastThread().name).isEqualTo("main")
            }
        }
    }
})