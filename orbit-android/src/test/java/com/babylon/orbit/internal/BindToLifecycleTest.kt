package com.babylon.orbit.internal

import androidx.lifecycle.Lifecycle
import com.babylon.orbit.MockLifecycleOwner
import io.reactivex.disposables.Disposables
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class BindToLifecycleTest {
    @Test
    fun `not disposed until corresponding event`() {
        val lifecycleOwner = MockLifecycleOwner()
        val disposable = Disposables.empty()

        lifecycleOwner.currentState = Lifecycle.State.INITIALIZED
        disposable.bindToLifecycle(lifecycleOwner)

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)

        assertThat(disposable.isDisposed).isFalse()
    }

    @Test
    fun `called in onCreate - disposed in onDestroy`() {
        val lifecycleOwner = MockLifecycleOwner()
        val disposable = Disposables.empty()

        lifecycleOwner.currentState = Lifecycle.State.INITIALIZED
        disposable.bindToLifecycle(lifecycleOwner)

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_CREATE)
        assertThat(disposable.isDisposed).isFalse()

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_DESTROY)
        assertThat(disposable.isDisposed).isTrue()
    }

    @Test
    fun `called in onStart - disposed in onStop`() {
        val lifecycleOwner = MockLifecycleOwner()
        val disposable = Disposables.empty()

        lifecycleOwner.currentState = Lifecycle.State.CREATED
        disposable.bindToLifecycle(lifecycleOwner)

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        assertThat(disposable.isDisposed).isFalse()

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)
        assertThat(disposable.isDisposed).isTrue()
    }

    @Test
    fun `called in onResume - disposed in onPause`() {
        val lifecycleOwner = MockLifecycleOwner()
        val disposable = Disposables.empty()

        lifecycleOwner.currentState = Lifecycle.State.STARTED
        disposable.bindToLifecycle(lifecycleOwner)

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        assertThat(disposable.isDisposed).isFalse()

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_PAUSE)
        assertThat(disposable.isDisposed).isTrue()
    }

    @Test
    fun `called after onResume - disposed in onPause`() {
        val lifecycleOwner = MockLifecycleOwner()
        val disposable = Disposables.empty()

        lifecycleOwner.currentState = Lifecycle.State.RESUMED
        disposable.bindToLifecycle(lifecycleOwner)

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_PAUSE)
        assertThat(disposable.isDisposed).isTrue()
    }

    @Test
    fun `IllegalStateException is thrown if lifecycle is already destroyed`() {
        val lifecycleOwner = MockLifecycleOwner()
        val disposable = Disposables.empty()

        lifecycleOwner.currentState = Lifecycle.State.DESTROYED

        assertThatThrownBy { disposable.bindToLifecycle(lifecycleOwner) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Lifecycle is already destroyed")
    }

    @Test
    fun `corresponding event is never dispatched`() {
        val lifecycleOwner = MockLifecycleOwner()
        val disposable = Disposables.empty()

        lifecycleOwner.currentState = Lifecycle.State.CREATED
        disposable.bindToLifecycle(lifecycleOwner)

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_DESTROY)
        assertThat(disposable.isDisposed).isTrue()
    }

    @Test
    fun `observer is removed after disposal`() {
        val lifecycleOwner = MockLifecycleOwner()
        val disposable = Disposables.empty()

        lifecycleOwner.currentState = Lifecycle.State.INITIALIZED
        disposable.bindToLifecycle(lifecycleOwner)

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_DESTROY)
        assertThat(lifecycleOwner.hasObservers).isFalse()
    }
}
