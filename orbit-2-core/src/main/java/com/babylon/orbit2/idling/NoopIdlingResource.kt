package com.babylon.orbit2.idling

class NoopIdlingResource : IdlingResource {
    override fun increment() = Unit
    override fun decrement() = Unit
    override fun close() = Unit
}
