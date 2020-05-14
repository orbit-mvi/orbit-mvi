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

package com.babylon.orbit2

object Orbit {

    private val defaultPlugins = setOf(BasePlugin)
    internal var plugins: Set<OrbitPlugin> = defaultPlugins
        private set

    fun registerDslPlugins(vararg plugins: OrbitPlugin) {
        val pluginSet = mutableSetOf<OrbitPlugin>(BasePlugin)
        pluginSet.addAll(plugins)

        Orbit.plugins = pluginSet.toSet()
    }

    fun resetPlugins() {
        plugins = defaultPlugins
    }

    fun requirePlugin(plugin: OrbitPlugin, componentName: String) {
        require(plugins.contains(plugin)) {
            throw IllegalStateException(
                "${plugin.javaClass.simpleName} required to use $componentName! " +
                        "Install plugins using Orbit.registerPlugins."
            )
        }
    }
}
