package org.orbitmvi.orbit.idling

public class NoopIdlingResource : IdlingResource {
    override fun increment(): Unit = Unit
    override fun decrement(): Unit = Unit
    override fun close(): Unit = Unit
}
