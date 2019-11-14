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
        val lifecycleOwner = MockLifecycleOwner()
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
                .reduce { currentState.copy(id = currentState.id + 1) }
        }
        val orbitViewModel = OrbitViewModel(middleware)

        // When I connect to the viewModel in onCreate
        orbitViewModel.connect(lifecycleOwner,
            stateConsumer = { stateSubject.onNext(it) },
            sideEffectConsumer = { sideEffectSubject.onNext(it) }
        )
        lifecycleOwner.dispatchEvent(Lifecycle.Event.ON_CREATE)
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
