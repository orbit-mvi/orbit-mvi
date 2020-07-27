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

import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.flow.Flow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class OrbitDslPluginsTest {
    val fixture = kotlinFixture()

    @AfterEach
    fun afterEach() {
        OrbitDslPlugins.reset()
    }

    @Test
    fun `base plugin is present by default`() {
        assertThat(OrbitDslPlugins.plugins).containsExactly(BaseDslPlugin)
    }

    @Test
    fun `base plugin is present after another plugin has been added`() {

        OrbitDslPlugins.register(TestPlugin)

        assertThat(OrbitDslPlugins.plugins).containsExactly(BaseDslPlugin, TestPlugin)
    }

    private object TestPlugin : OrbitDslPlugin {
        override fun <S : Any, E : Any, SE : Any> apply(
            containerContext: OrbitDslPlugin.ContainerContext<S, SE>,
            flow: Flow<E>,
            operator: Operator<S, E>,
            createContext: (event: E) -> Context<S, E>
        ): Flow<Any> {
            return flow
        }
    }
}
