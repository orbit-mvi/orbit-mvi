package com.babylon.orbit2.idling

interface IdlingResource {
    fun increment()
    fun decrement()
    fun close()
}
