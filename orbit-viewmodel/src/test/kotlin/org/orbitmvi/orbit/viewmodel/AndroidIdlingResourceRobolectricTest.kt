/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.viewmodel

import androidx.test.espresso.Espresso
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.orbitmvi.orbit.container
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class AndroidIdlingResourceRobolectricTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @After
    fun after() {
        scope.cancel()
    }

    @Test
    fun `idling resources have unique names`() {
        scope.container<TestState, Int>(
            initialState = TestState(0),
            buildSettings = { idlingRegistry = AndroidIdlingResource() }
        )

        scope.container<TestState, Int>(
            initialState = TestState(0),
            buildSettings = { idlingRegistry = AndroidIdlingResource() }
        )

        forceIdlingResourceSync()

        @Suppress("DEPRECATION")
        assertEquals(2, Espresso.getIdlingResources().size)
    }

    /**
     * Force IdlingRegistry to be synced into Espresso.baseRegistry
     */
    private fun forceIdlingResourceSync() {
        CountingIdlingResource("bob").apply {
            @Suppress("DEPRECATION")
            Espresso.registerIdlingResources(this)
            @Suppress("DEPRECATION")
            Espresso.unregisterIdlingResources(this)
        }
    }

    data class TestState(val value: Int)
}
