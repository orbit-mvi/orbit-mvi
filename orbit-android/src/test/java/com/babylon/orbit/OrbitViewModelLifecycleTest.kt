package com.babylon.orbit

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.ref.WeakReference

@RunWith(RobolectricTestRunner::class)
class OrbitViewModelLifecycleTest {

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
    fun `If I connect in onCreate I get disconnected in onDestroy`() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        RxAndroidPlugins.setMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        lateinit var orbitViewModel: OrbitViewModel<TestState, String>
        val lifecycle = LifecycleRegistry(mock())
        val lifecycleOwner = LifecycleOwner { lifecycle }
        val stateSubject = PublishSubject.create<TestState>()
        val stateObserver = stateSubject.test()
        val sideEffectSubject = PublishSubject.create<String>()
        val sideEffectObserver = sideEffectSubject.test()

        // Given A middleware with no flows
        val middleware = createTestMiddleware {
            perform("send side effect")
                .on<Unit>()
                .sideEffect { post("foobar") }
                .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
        }
        orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the view model in onCreate
        orbitViewModel.connect(lifecycleOwner,
            stateConsumer = { stateSubject.onNext(it) },
            sideEffectConsumer = { sideEffectSubject.onNext(it) }
        )
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        stateObserver.awaitCount(1)

        // Then I receive the initial state
        assertThat(stateObserver.values()).containsExactly(middleware.initialState)

        // When I transition through to stopped
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

        // And I send an action to the container
        orbitViewModel.sendAction(Unit)
        stateObserver.awaitCount(2)
        sideEffectObserver.awaitCount(1)

        // Then I receive the updated state
        assertThat(stateObserver.values())
            .containsExactly(middleware.initialState, TestState(43))

        // And I receive the side effect
        assertThat(sideEffectObserver.values())
            .containsExactly("foobar")

        // When I transition through to destroyed
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // And I send an action to the container
        orbitViewModel.sendAction(Unit)
        stateObserver.awaitCount(3)

        // Then I do not receive further updates
        assertThat(stateObserver.values())
            .containsExactly(middleware.initialState, TestState(43))

        // And I do not receive further side effects
        assertThat(sideEffectObserver.values())
            .containsExactly("foobar")
    }

    @Test
    fun `If I connect in onStart I get disconnected in onStop`() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        RxAndroidPlugins.setMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        lateinit var orbitViewModel: OrbitViewModel<TestState, String>
        val lifecycle = LifecycleRegistry(mock())
        val lifecycleOwner = LifecycleOwner { lifecycle }
        val stateSubject = PublishSubject.create<TestState>()
        val stateObserver = stateSubject.test()
        val sideEffectSubject = PublishSubject.create<String>()
        val sideEffectObserver = sideEffectSubject.test()

        // Given A middleware with no flows
        val middleware = createTestMiddleware {
            perform("send side effect")
                .on<Unit>()
                .sideEffect { post("foobar") }
                .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
        }
        orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the view model in onStart
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        orbitViewModel.connect(lifecycleOwner,
            stateConsumer = { stateSubject.onNext(it) },
            sideEffectConsumer = { sideEffectSubject.onNext(it) }
        )
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        stateObserver.awaitCount(1)

        // Then I receive the initial state
        assertThat(stateObserver.values()).containsExactly(middleware.initialState)

        // When I transition through to paused
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

        // And I send an action to the container
        orbitViewModel.sendAction(Unit)
        stateObserver.awaitCount(2)

        // Then I receive the updated state
        assertThat(stateObserver.values())
            .containsExactly(middleware.initialState, TestState(43))

        // And I receive the side effect
        assertThat(sideEffectObserver.values())
            .containsExactly("foobar")

        // When I transition through to stopped
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

        // And I send an action to the container
        orbitViewModel.sendAction(Unit)
        stateObserver.awaitCount(3)

        // Then I do not receive further updates
        assertThat(stateObserver.values())
            .containsExactly(middleware.initialState, TestState(43))

        // And I do not receive further side effects
        assertThat(sideEffectObserver.values())
            .containsExactly("foobar")
    }

    @Test
    fun `If I connect in onResume I get disconnected in onPause`() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        RxAndroidPlugins.setMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        lateinit var orbitViewModel: OrbitViewModel<TestState, String>
        val lifecycle = LifecycleRegistry(mock())
        val lifecycleOwner = LifecycleOwner { lifecycle }
        val stateSubject = PublishSubject.create<TestState>()
        val stateObserver = stateSubject.test()
        val sideEffectSubject = PublishSubject.create<String>()
        val sideEffectObserver = sideEffectSubject.test()

        // Given A middleware with no flows
        val middleware = createTestMiddleware {
            perform("send side effect")
                .on<Unit>()
                .sideEffect { post("foobar") }
                .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
        }
        orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the view model in onResume
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        orbitViewModel.connect(lifecycleOwner,
            stateConsumer = { stateSubject.onNext(it) },
            sideEffectConsumer = { sideEffectSubject.onNext(it) }
        )
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        stateObserver.awaitCount(1)

        // When I send an action to the container
        orbitViewModel.sendAction(Unit)
        stateObserver.awaitCount(2)

        // Then I receive the updated state
        assertThat(stateObserver.values())
            .containsExactly(middleware.initialState, TestState(43))

        // And I receive the side effect
        assertThat(sideEffectObserver.values())
            .containsExactly("foobar")

        // When I transition through to paused
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)

        // And I send an action to the container
        orbitViewModel.sendAction(Unit)
        stateObserver.awaitCount(2)

        // Then I do not receive further updates
        assertThat(stateObserver.values())
            .containsExactly(middleware.initialState, TestState(43))

        // And I do not receive further side effects
        assertThat(sideEffectObserver.values())
            .containsExactly("foobar")
    }

    @Test
    fun `Instance of view is not retained after disconnection`() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        RxAndroidPlugins.setMainThreadSchedulerHandler {
            RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
        }
        lateinit var orbitViewModel: OrbitViewModel<TestState, String>
        lateinit var weakConsumer: WeakReference<Consumer>
        val lifecycle = LifecycleRegistry(mock())
        val lifecycleOwner = LifecycleOwner { lifecycle }
        val stateSubject = PublishSubject.create<TestState>()
        val stateObserver = stateSubject.test()
        val sideEffectSubject = PublishSubject.create<String>()

        // Given A middleware with no flows
        val middleware = createTestMiddleware {
            perform("send side effect")
                .on<Unit>()
                .sideEffect { post("foobar") }
                .withReducer { getCurrentState().copy(id = getCurrentState().id + 1) }
        }
        orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the view model in onStart
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        weakConsumer = WeakReference(Consumer(stateSubject, sideEffectSubject)).also {
            orbitViewModel.connect(
                lifecycleOwner,
                it.get()!!::consumeState,
                it.get()!!::consumeSideEffect
            )
        }

        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        stateObserver.awaitCount(1)

        // Then I receive the initial state
        assertThat(stateObserver.values()).containsExactly(middleware.initialState)

        // When I transition through to stopped
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

        // Then I expect the consumer to be cleared
        var cleared = false
        for (i in 0..15) {
            System.gc()
            Runtime.getRuntime().gc()
            if (!cleared) {
                cleared = weakConsumer.get() == null
                Thread.sleep(1000)
            } else break
        }
        assertThat(cleared).isTrue()
    }
}

internal class Consumer(
    private val stateSubject: PublishSubject<TestState>,
    private val sideEffectSubject: PublishSubject<String>
) {
    fun consumeState(state: TestState) = stateSubject.onNext(state)
    fun consumeSideEffect(sideEffect: String) = sideEffectSubject.onNext(sideEffect)

}