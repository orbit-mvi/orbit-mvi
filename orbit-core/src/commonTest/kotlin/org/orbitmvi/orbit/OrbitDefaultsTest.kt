/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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
 */

package org.orbitmvi.orbit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OrbitDefaultsTest {

    @BeforeTest
    fun resetBefore() {
        Orbit.resetDefaults()
    }

    @AfterTest
    fun resetAfter() {
        Orbit.resetDefaults()
    }

    @Test
    fun `configureDefaults changes settings of subsequently created containers`() = runTest {
        Orbit.configureDefaults {
            sideEffectMode = SideEffectMode.FAN_OUT
            eventLoopDispatcher = Dispatchers.Unconfined
        }

        val container = backgroundScope.orbitContainer<Unit, Nothing>(Unit)

        assertEquals(SideEffectMode.FAN_OUT, container.settings.sideEffectMode)
        assertEquals(Dispatchers.Unconfined, container.settings.eventLoopDispatcher)
    }

    @Test
    fun `per-container buildSettings overrides global default`() = runTest {
        Orbit.configureDefaults {
            sideEffectMode = SideEffectMode.FAN_OUT
        }

        val container = backgroundScope.orbitContainer<Unit, Nothing>(
            initialState = Unit,
            buildSettings = { sideEffectMode = SideEffectMode.BROADCAST }
        )

        assertEquals(SideEffectMode.BROADCAST, container.settings.sideEffectMode)
    }

    @Test
    fun `configureDefaults is idempotent and not cumulative`() = runTest {
        Orbit.configureDefaults {
            sideEffectMode = SideEffectMode.FAN_OUT
        }
        // A second call should start from library defaults, not accumulate the first call's changes.
        Orbit.configureDefaults {
            repeatOnSubscribedStopTimeout = 500L
        }

        val defaults = RealSettings()
        assertEquals(defaults.sideEffectMode, Orbit.defaultSettings.sideEffectMode)
        assertEquals(500L, Orbit.defaultSettings.repeatOnSubscribedStopTimeout)
    }

    @Test
    fun `resetDefaults restores library defaults`() = runTest {
        Orbit.configureDefaults {
            sideEffectMode = SideEffectMode.FAN_OUT
        }

        Orbit.resetDefaults()

        assertEquals(RealSettings().sideEffectMode, Orbit.defaultSettings.sideEffectMode)
    }

    @Test
    fun `SettingsBuilder dispatcher properties round-trip into RealSettings`() {
        val settings = SettingsBuilder().apply {
            eventLoopDispatcher = Dispatchers.Unconfined
            intentLaunchingDispatcher = Dispatchers.Default
        }.settings

        assertEquals(Dispatchers.Unconfined, settings.eventLoopDispatcher)
        assertEquals(Dispatchers.Default, settings.intentLaunchingDispatcher)
    }
}
