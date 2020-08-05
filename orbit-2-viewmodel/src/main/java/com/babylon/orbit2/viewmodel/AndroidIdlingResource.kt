package com.babylon.orbit2.viewmodel

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.net.UriIdlingResource
import com.babylon.orbit2.idling.IdlingResource

class AndroidIdlingResource : IdlingResource {
    private val otherResource = UriIdlingResource("orbit-mvi", 100)

    init {
        IdlingRegistry.getInstance().register(otherResource)
    }

    override fun increment() {
        otherResource.beginLoad("uri")
    }

    override fun decrement() {
        otherResource.endLoad("uri")
    }

    override fun close() {
        IdlingRegistry.getInstance().unregister(otherResource)
    }
}
