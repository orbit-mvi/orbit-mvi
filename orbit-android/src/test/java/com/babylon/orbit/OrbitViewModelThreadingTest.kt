package com.babylon.orbit

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OrbitViewModelThreadingTest {

    @Before
    fun before() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        RxAndroidPlugins.setMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
    }

    @After
    fun after() {
        RxJavaPlugins.reset()
    }

    @Test
    fun `Side effects and state updates are received on the android main thread`() {
        val lifecycle = LifecycleRegistry(mock())
        val lifecycleOwner = LifecycleOwner { lifecycle }
        val stateSubject = PublishSubject.create<TestState>()
        val stateObserver = stateSubject.test()
        val sideEffectSubject = PublishSubject.create<String>()
        val sideEffectObserver = sideEffectSubject.test()

        // Given an android view model with a simple middleware
        val middleware = createTestMiddleware {
            perform("send side effect")
                .on<Unit>()
                .sideEffect { post("foo") }
                .sideEffect { post("bar") }
                .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
        }
        val orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the viewModel in onCreate
        orbitViewModel.connect(lifecycleOwner,
            stateConsumer = { stateSubject.onNext(it) },
            sideEffectConsumer = { sideEffectSubject.onNext(it) }
        )
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        stateObserver.awaitCount(1)

        // When I send an event to the container
        orbitViewModel.sendAction(Unit)
        stateObserver.awaitCount(2)

        // Then the state observer listens on the android main thread
        assertThat(stateObserver.lastThread().name).isEqualTo("main")

        // And The side effect observer listens on the android main thread
        assertThat(sideEffectObserver.lastThread().name).isEqualTo("main")
    }

}