/*
 * Copyright 2019 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit

import androidx.lifecycle.Lifecycle
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.ref.WeakReference

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
        lateinit var orbitViewModel: OrbitViewModel<TestState, String>
        val lifecycleOwner = MockLifecycleOwner()
        val stateSubject = PublishSubject.create<TestState>()
        val stateObserver = stateSubject.test()
        val sideEffectSubject = PublishSubject.create<String>()
        val sideEffectObserver = sideEffectSubject.test()

        // Given A middleware with no flows
        val middleware = createTestMiddleware {
            perform("send side effect")
                .on<Unit>()
                .sideEffect { post("foobar") }
                .reduce { currentState.copy(id = currentState.id + 1) }
        }
        orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the view model in onCreate
        orbitViewModel.connect(lifecycleOwner,
            stateConsumer = { stateSubject.onNext(it) },
            sideEffectConsumer = { sideEffectSubject.onNext(it) }
        )
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_CREATE)
        stateObserver.awaitCount(1)

        // Then I receive the initial state
        assertThat(stateObserver.values()).containsExactly(middleware.initialState)

        // When I transition through to stopped
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)

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
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_DESTROY)

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
        lateinit var orbitViewModel: OrbitViewModel<TestState, String>
        val lifecycleOwner = MockLifecycleOwner()
        val stateSubject = PublishSubject.create<TestState>()
        val stateObserver = stateSubject.test()
        val sideEffectSubject = PublishSubject.create<String>()
        val sideEffectObserver = sideEffectSubject.test()

        // Given A middleware with no flows
        val middleware = createTestMiddleware {
            perform("send side effect")
                .on<Unit>()
                .sideEffect { post("foobar") }
                .reduce { currentState.copy(id = currentState.id + 1) }
        }
        orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the view model in onStart
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_CREATE)
        orbitViewModel.connect(lifecycleOwner,
            stateConsumer = { stateSubject.onNext(it) },
            sideEffectConsumer = { sideEffectSubject.onNext(it) }
        )
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        stateObserver.awaitCount(1)

        // Then I receive the initial state
        assertThat(stateObserver.values()).containsExactly(middleware.initialState)

        // When I transition through to paused
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_PAUSE)

        // And I send an action to the container
        orbitViewModel.sendAction(Unit)
        stateObserver.awaitCount(2)

        // Then I receive the updated state
        assertThat(stateObserver.values())
            .containsExactly(middleware.initialState, TestState(43))

        // And I receive the side effect
        sideEffectObserver.awaitCount(1)
        assertThat(sideEffectObserver.values())
            .containsExactly("foobar")

        // When I transition through to stopped
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)

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
        lateinit var orbitViewModel: OrbitViewModel<TestState, String>
        val lifecycleOwner = MockLifecycleOwner()
        val stateSubject = PublishSubject.create<TestState>()
        val stateObserver = stateSubject.test()
        val sideEffectSubject = PublishSubject.create<String>()
        val sideEffectObserver = sideEffectSubject.test()

        // Given A middleware with no flows
        val middleware = createTestMiddleware {
            perform("send side effect")
                .on<Unit>()
                .sideEffect { post("foobar") }
                .reduce { currentState.copy(id = currentState.id + 1) }
        }
        orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the view model in onResume
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        orbitViewModel.connect(lifecycleOwner,
            stateConsumer = { stateSubject.onNext(it) },
            sideEffectConsumer = { sideEffectSubject.onNext(it) }
        )
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
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
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_PAUSE)

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
        lateinit var orbitViewModel: OrbitViewModel<TestState, String>
        lateinit var weakConsumer: WeakReference<Consumer>
        val lifecycleOwner = MockLifecycleOwner()
        val stateSubject = PublishSubject.create<TestState>()
        val stateObserver = stateSubject.test()
        val sideEffectSubject = PublishSubject.create<String>()

        // Given A middleware with no flows
        val middleware = createTestMiddleware {
            perform("send side effect")
                .on<Unit>()
                .sideEffect { post("foobar") }
                .reduce { currentState.copy(id = currentState.id + 1) }
        }
        orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the view model in onStart
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_CREATE)
        weakConsumer = WeakReference(Consumer(stateSubject, sideEffectSubject)).also {
            orbitViewModel.connect(
                lifecycleOwner,
                it.get()!!::consumeState,
                it.get()!!::consumeSideEffect
            )
        }

        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_START)
        stateObserver.awaitCount(1)

        // Then I receive the initial state
        assertThat(stateObserver.values()).containsExactly(middleware.initialState)

        // When I transition through to stopped
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_RESUME)
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_STOP)

        // Then I expect the consumer to be cleared
        var cleared = false
        @Suppress("ExplicitGarbageCollectionCall")
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
