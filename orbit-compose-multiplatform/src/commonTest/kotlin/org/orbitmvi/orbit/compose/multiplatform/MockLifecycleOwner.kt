package org.orbitmvi.orbit.compose.multiplatform

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

internal class MockLifecycleOwner : LifecycleOwner {
    private val registry = LifecycleRegistry(this)

    var currentState: Lifecycle.State
        get() = registry.currentState
        set(value) {
            registry.currentState = value
        }

    val hasObservers: Boolean
        get() = registry.observerCount > 0

    fun dispatchEvent(event: Lifecycle.Event) {
        registry.handleLifecycleEvent(event)
    }

    override val lifecycle: Lifecycle
        get() = registry
}
