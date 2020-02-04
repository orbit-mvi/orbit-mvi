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

import androidx.lifecycle.SavedStateHandle
import com.appmattus.kotlinfixture.kotlinFixture
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class OrbitViewModelStateSavingTest {

    private val fixture = kotlinFixture()

    private val testRange = -65536..65536

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
    fun `If I provide a saved state handle for a middleware initialised ViewModel my state gets saved`() {
        val savedStateHandle = SavedStateHandle()
        val initialState = fixture(testRange)
        val addition = fixture(testRange)

        // Given A simple middleware with a reducer
        val middleware = middleware<Int, Unit>(initialState) {
            perform("Increment id")
                .on<Unit>()
                .reduce { currentState + addition }
        }

        testSavedState(
            OrbitViewModel(middleware, savedStateHandle),
            savedStateHandle,
            initialState,
            addition
        )
    }

    @Test
    fun `If I provide a saved state handle for a self initialised ViewModel my state gets saved`() {
        val savedStateHandle = SavedStateHandle()
        val initialState = fixture(testRange)
        val addition = fixture(testRange)

        // Given A simple middleware with a reducer
        val orbitViewModel = OrbitViewModel<Int, Unit>(initialState, savedStateHandle) {
            perform("Increment id")
                .on<Unit>()
                .reduce { currentState + addition }
        }

        testSavedState(orbitViewModel, savedStateHandle, initialState, addition)
    }

    private fun testSavedState(
        orbitViewModel: OrbitViewModel<Int, *>,
        savedStateHandle: SavedStateHandle,
        initialState: Int,
        addition: Int
    ) {
        val lifecycleOwner = MockLifecycleOwner()
        val stateSubject = PublishSubject.create<Int>()
        val stateObserver = stateSubject.test()

        // Given I am connected to the view model
        orbitViewModel.connect(lifecycleOwner) { stateSubject.onNext(it) }

        // When I post an action to the viewModel
        orbitViewModel.sendAction(Unit)

        // Then My new state is saved
        stateObserver.awaitCount(2)
        assertThat(savedStateHandle.get<Int?>("state"))
            .isEqualTo(initialState + addition)
    }
}
