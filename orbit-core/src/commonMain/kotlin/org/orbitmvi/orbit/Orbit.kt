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

import kotlin.concurrent.Volatile

/**
 * Global Orbit configuration.
 */
public object Orbit {
    /**
     * The global default settings every new container inherits, before any per-container
     * [SettingsBuilder] overrides are applied. Defaults to the library defaults ([RealSettings]).
     */
    @Volatile
    public var defaultSettings: RealSettings = RealSettings()
        private set

    /**
     * Configure the global default container settings. Apply once at app startup, before any
     * containers are created. Always starts from the library defaults, so repeated calls are
     * idempotent (not cumulative).
     */
    public fun configureDefaults(buildSettings: SettingsBuilder.() -> Unit) {
        defaultSettings = SettingsBuilder(RealSettings()).apply(buildSettings).settings
    }

    /** Reset global defaults back to library defaults. Mainly for tests. */
    public fun resetDefaults() {
        defaultSettings = RealSettings()
    }
}
