package com.babylon.orbit

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

class OrbitViewModelSpek : Spek({
    Feature("View Model - State") {
        Scenario("The current state can be queried") {}
    }
    Feature("View Model - Lifecycle") {
        Scenario("If I connect in onCreate I get disconnected in onDestroy") {}
        Scenario("If I connect in onStart I get disconnected in onStop") {}
        Scenario("If I connect in onResume I get disconnected in onPause") {}
        Scenario("If I connect in methods other than onCreate/onStart/onResume I get an exception") {}
        // TODO think if above makes sense in context of fragment lifecycle
    }
    Feature("View Model - Connection") {
        Scenario("Actions are delivered to the orbit container even if view is not connected") {}
        Scenario("I receive state updates and side effects when connected") {}
        Scenario("I do not receive state updates and side effects when disconnected") {}
        Scenario("Instance of view is not retained after disconnection") {} // How to test this
    }
})