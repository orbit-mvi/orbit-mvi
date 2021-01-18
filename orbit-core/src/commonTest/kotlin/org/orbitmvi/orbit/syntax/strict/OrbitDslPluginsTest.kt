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

package org.orbitmvi.orbit.syntax.strict

import org.orbitmvi.orbit.syntax.Operator
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.Flow
import kotlin.test.AfterTest
import kotlin.test.Test

internal class OrbitDslPluginsTest {

    @AfterTest
    fun afterEach() {
        orbitDslPlugins.reset()
    }

    @Test
    fun `base plugin is present by default`() {
        orbitDslPlugins.plugins.shouldContainExactly(BaseDslPlugin)
    }

    @Test
    fun `base plugin is present after another plugin has been added`() {

        orbitDslPlugins.register(TestPlugin)

        orbitDslPlugins.plugins.shouldContainExactly(BaseDslPlugin, TestPlugin)
    }

    private object TestPlugin : OrbitDslPlugin {
        override fun <S : Any, E, SE : Any> apply(
            containerContext: OrbitDslPlugin.ContainerContext<S, SE>,
            flow: Flow<E>,
            operator: Operator<S, E>,
            createContext: (event: E) -> VolatileContext<S, E>
        ): Flow<Any?> {
            return flow
        }
    }
}
