/*
 * Copyright 2020 Babylon Partners Limited
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

package org.orbitmvi.orbit.syntax.strict

/**
 * Orbit DSL plugin registry. In order for DSL extensions to work they need to be registered here
 * before use.
 *
 * The base DSL provided by [BaseDslPlugin] does not have to be registered as it is registered
 * implicitly, even after a [reset].
 */
public object OrbitDslPlugins {

    internal var plugins: Set<OrbitDslPlugin> = setOf(BaseDslPlugin)
        private set

    /**
     * Register DSL plugins. This has to be done before using the DSL provided by the plugin.
     *
     * @param plugin The DSL plugin to register
     */
    public fun register(plugin: OrbitDslPlugin) {
        if (!plugins.contains(plugin)) {
            plugins = plugins + plugin
        }
    }

    /**
     * Clears all registered plugins apart from the [BaseDslPlugin].
     */
    public fun reset() {
        plugins = setOf(BaseDslPlugin)
    }
}
