package org.orbitmvi.orbit.idling

public interface IdlingResource {
    public fun increment()
    public fun decrement()
    public fun close()
}
