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

package com.babylon.orbit.sample.di

import androidx.lifecycle.SavedStateHandle
import com.babylon.orbit.sample.presentation.TodoViewModel
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class CheckKoinTest : KoinTest {

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
    fun `check module hierarchy`() {
        koinApplication {
            modules(listOf(domainModule, presentationModule))
        }
            .checkModules {
                create<TodoViewModel> {
                    parametersOf(mock<SavedStateHandle>())
                }
            }
    }
}
