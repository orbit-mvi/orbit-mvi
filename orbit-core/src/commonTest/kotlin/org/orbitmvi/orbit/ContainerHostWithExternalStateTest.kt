/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.annotation.OrbitExperimental
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerHostWithExternalStateTest {

    private sealed interface Item {
        data class Internal(val value: Int) : Item
        data class External(val value: String) : Item
    }

    @Test
    fun `external state flow is mapped correctly`() = runTest {
        with(TestHost(backgroundScope)) {
            merge(
                container.stateFlow.map(Item::Internal),
                container.externalStateFlow.map(Item::External)
            ).test {
                assertEquals(Item.Internal(0), awaitItem())
                assertEquals(Item.External("0"), awaitItem())

                val value = Random.nextInt(1, 65535)
                newState(value)

                assertEquals(Item.Internal(value), awaitItem())
                assertEquals(Item.External(value.toString()), awaitItem())
            }
        }
    }

    @Test
    fun `ref count external state flow is mapped correctly`() = runTest {
        with(TestHost(backgroundScope)) {
            merge(
                container.refCountStateFlow.map(Item::Internal),
                container.externalRefCountStateFlow.map(Item::External)
            ).test {
                assertEquals(Item.Internal(0), awaitItem())
                assertEquals(Item.External("0"), awaitItem())

                val value = Random.nextInt(1, 65535)
                newState(value)

                assertEquals(Item.Internal(value), awaitItem())
                assertEquals(Item.External(value.toString()), awaitItem())
            }
        }
    }

    @Test
    fun `external state flow is distinct`() = runTest {
        with(TestHost(backgroundScope)) {
            merge(
                container.stateFlow.map(Item::Internal),
                container.externalStateFlow.map(Item::External)
            ).test {
                assertEquals(Item.Internal(0), awaitItem())
                assertEquals(Item.External("0"), awaitItem())

                newState(65536)

                assertEquals(Item.Internal(65536), awaitItem())
                // No new External item is emitted as it is the same as the previous one
            }
        }
    }

    @Test
    fun `ref count external state flow is distinct`() = runTest {
        with(TestHost(backgroundScope)) {
            merge(
                container.refCountStateFlow.map(Item::Internal),
                container.externalRefCountStateFlow.map(Item::External)
            ).test {
                assertEquals(Item.Internal(0), awaitItem())
                assertEquals(Item.External("0"), awaitItem())

                newState(65536)

                assertEquals(Item.Internal(65536), awaitItem())
                // No new External item is emitted as it is the same as the previous one
            }
        }
    }

    @Test
    fun `side effect posts`() = runTest {
        with(TestHost(backgroundScope)) {
            container.sideEffectFlow.test {
                val value = Random.nextInt(1, 65535)
                newSideEffect(value)

                assertEquals(value, awaitItem())
            }
        }
    }

    class TestHost(backgroundScope: CoroutineScope) : ContainerHostWithExternalState<Int, String, Int> {
        override val container = backgroundScope.container<Int, Int>(initialState = 0).withExternalState(::transformState)

        fun newState(action: Int) = intent { subIntent(action) }

        fun newSideEffect(action: Int) = intent { postSideEffect(action) }

        @OptIn(OrbitExperimental::class)
        private suspend fun subIntent(action: Int) = subIntent { reduce { action } }

        private fun transformState(internalState: Int): String {
            return internalState.mod(65536).toString()
        }
    }
}
