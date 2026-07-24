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

package org.orbitmvi.orbit.idling

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitInternal
import org.orbitmvi.orbit.internal.repeatonsubscription.TestSubscribedCounter
import org.orbitmvi.orbit.syntax.ContainerContext
import org.orbitmvi.orbit.syntax.Operator
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(OrbitInternal::class)
internal class OperatorIdlingExtensionsTest {

    private val idlingResource = TestIdlingResource()

    private val containerContext = ContainerContext<Int, Int>(
        settings = RealSettings(idlingRegistry = idlingResource),
        postSideEffect = {},
        reduce = {},
        subscribedCounter = TestSubscribedCounter(),
        stateFlow = MutableStateFlow(0),
    )

    @Test
    fun idling_is_registered_around_the_block() = runTest {
        val result = containerContext.withIdling(TestOperator(registerIdling = true)) {
            assertEquals(1, idlingResource.counter())
            "result"
        }

        assertEquals("result", result)
        assertEquals(0, idlingResource.counter())
    }

    @Test
    fun idling_is_not_registered_when_disabled() = runTest {
        containerContext.withIdling(TestOperator(registerIdling = false)) {
            assertEquals(0, idlingResource.counter())
        }

        assertEquals(0, idlingResource.counter())
    }

    @Test
    fun idling_is_decremented_when_the_block_throws() = runTest {
        assertFailsWith<IllegalStateException> {
            containerContext.withIdling(TestOperator(registerIdling = true)) {
                error("boom")
            }
        }

        assertEquals(0, idlingResource.counter())
    }

    private class TestOperator(override val registerIdling: Boolean) : Operator<Int, Int>

    private class TestIdlingResource : IdlingResource {
        private val counter = AtomicInt(0)

        override fun increment() {
            counter.incrementAndFetch()
        }

        override fun decrement() {
            counter.decrementAndFetch()
        }

        override fun close() = Unit

        fun counter() = counter.load()
    }
}
