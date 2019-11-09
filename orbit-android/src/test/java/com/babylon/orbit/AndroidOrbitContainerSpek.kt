package com.babylon.orbit

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

class AndroidOrbitContainerSpek : Spek({
    Feature("Android Container - Threading") {
        Scenario("Side effects are received on the android main thread") {}
        Scenario("State updates are received on the android main thread") {}
    }
})