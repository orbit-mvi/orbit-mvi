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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class OrbitViewModelStateReadingTest {

    private val fixture = kotlinFixture()

    companion object {
        @BeforeAll
        @JvmStatic
        fun before() {
            RxAndroidPlugins.setInitMainThreadSchedulerHandler {
                RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
            }
            RxAndroidPlugins.setMainThreadSchedulerHandler {
                RxJavaPlugins.createNewThreadScheduler { Thread(it, "main") }
            }
        }

        @AfterAll
        @JvmStatic
        fun after() {
            RxJavaPlugins.reset()
        }
    }

    @Test
    fun `If I provide a saved state handle with a saved state for a middleware initialised ViewModel my state gets read`() {
        val savedState = fixture<Int>()
        val savedStateHandle = SavedStateHandle(mapOf("state" to savedState))

        // Given A simple middleware with a reducer
        val middleware = middleware<Int, Unit>(fixture()) {}

        testReadState(OrbitViewModel(middleware, savedStateHandle), savedState)
    }

    @Test
    fun `If I provide a saved state handle with a saved state for a self initialised ViewModel my state gets read`() {
        val savedState = fixture<Int>()
        val savedStateHandle = SavedStateHandle(mapOf("state" to savedState))

        // Given A simple middleware with a reducer
        val orbitViewModel = OrbitViewModel<Int, Unit>(fixture(), savedStateHandle) {}

        testReadState(orbitViewModel, savedState)
    }

    private fun testReadState(
        orbitViewModel: OrbitViewModel<Int, *>,
        expectedState: Int
    ) {
        val lifecycleOwner = MockLifecycleOwner()
        val stateSubject = PublishSubject.create<Int>()
        val stateObserver = stateSubject.test()

        // When I connect to the view model
        orbitViewModel.connect(lifecycleOwner) { stateSubject.onNext(it) }

        // Then My saved state is emitted
        stateObserver.awaitCount(1)
            .assertValue(expectedState)
    }
}
